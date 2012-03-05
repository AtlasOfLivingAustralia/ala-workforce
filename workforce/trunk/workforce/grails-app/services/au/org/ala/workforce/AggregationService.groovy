package au.org.ala.workforce

import grails.converters.JSON

class AggregationService {

    static transactional = false

    def modelLoaderService
    
    public void getAggregatedData(QuestionModel question, QuestionSet qset, List aggregatedData) {
        if (question.aggregations) {
            aggregatedData.addAll(getData(question, qset))
        }
        for (QuestionModel q : question.questions) {
            getAggregatedData(q, qset, aggregatedData)
        }
    }

    private List getData(QuestionModel q, QuestionSet qset) {
        def chartData = []

        def descValueComp = [
            compare: { a, b ->
                def valA = a[1] as float
                def valB = b[1] as float
                valA == valB ? 0 : valA > valB ? -1 : 1 } ] as Comparator

        def ascKeyComp = [
            compare: { a, b ->
                def valA = a[0] as String
                def valB = b[0] as String
                valA.equals(valB) ? 0 : valA < valB ? -1 : 1 } ] as Comparator

        q.aggregations.each {
            def year = ConfigData.getSurveyYear()
            Map chart = [
                    title: it.title ?: (q.shorttext ?: q.qtext),
                    subtitle: year,
                    chart: it.chart,
                    xAxisTitle: it.xAxisTitle,
                    xAxisLabelOrientation: it.xAxisLabelOrientation,
                    size: it.size,
                    stacked: it.stacked,
                    legend: it.legend,
                    legendAlignment: it.legendAlignment]
            if (it.chart != 'pie') {
                chart['yAxisLabel'] = it.yAxisLabel
            }

            switch (it.type) {
                case 'countByAnswer':
                    def guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    Map counts = Answer.getAnswerCounts(guids, year, it.answer == 'qtext')

                    def data = [:]
                    def categories = []
                    def values = []

                    switch (it.result) {
                        case 'percentage':
                            def total = 0
                            counts.each { key, value ->
                                total += value
                            }
                            counts.each { key, value ->
                                def label = initialUpper(key as String)
                                categories << label
                                values << percentage(value,total)
                            }
                            break

                        default:
                            if (it.dataType?.equalsIgnoreCase('importance')) {
                                def importanceScale = getImportanceScale()
                                def noOfSurveys = Event.numberCompletedForYear(qset.setId, year as String)
                                def noOfAnswers = 0
                                counts.each { key, value ->
                                    noOfAnswers += value
                                }
                                importanceScale.each { key, name ->
                                    categories << name
                                    if (counts[(key)]) {
                                        values << counts[(key)]
                                    } else {
                                        values << 0
                                    }
                                }
                                categories << 'Not important (not rated)'
                                values << noOfSurveys - noOfAnswers
                            } else {
                                counts.each { key, value ->
                                    categories << initialUpper(key as String)
                                    values << value
                                }
                            }
                            break
                    }
                    data['categories'] = categories
                    data['series'] = [[year as String, values]]
                    chart['data'] = data
                    break

                case 'countByAnswerByAgeGroup':
                    def guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    def ageGroupQuestionNo = qset.getKnownQuestionNumber('ageGroup')
                    def ageGroupQuestion = modelLoaderService.loadQuestion(qset.setId, ageGroupQuestionNo as int)
                    Map answers = Answer.getAnswersByUserByAgeGroup(guids, ageGroupQuestion.guid, year, it.answer == 'qtext')
                    def ageGroups = getRangeLabels(ageGroupQuestion)
                    List categories = []
                    if (q.qtype == QuestionType.pick) {
                        categories = q.adata.clone() as List
                    } else if (q.qtype == QuestionType.rank || q.owner.qtype == QuestionType.rank) {
                        categories = getImportanceScale().keySet().toList()
                    }

                    def counts = [:]  // Map of counts per age group within map of answers
                    answers.each { it2 ->
                        def category = null
                        def ageGroup = null
                        it2.value.each { guid, answer ->
                            if (guid.equals(ageGroupQuestion.guid)) {
                                ageGroup = answer['answer']
                            } else {
                                category = answer['answer']
                            }

                            if (category && ageGroup) {
                                if (inSubset(category, ageGroup, it.subset)) {
                                    if (counts[category]) {
                                        counts[category][ageGroup]++
                                    } else {
                                        def ageGroupCounts = [:]
                                        ageGroups.each { age ->
                                            ageGroupCounts[age] = 0
                                        }
                                        ageGroupCounts[ageGroup]++
                                        counts[category] = ageGroupCounts
                                    }
                                }
                            }
                        }
                    }

                    def series = []

                    if (it.order?.equalsIgnoreCase('desc')) {
                        Collections.reverse(categories)
                    }

                    def total = 0
                    if (it.result.equalsIgnoreCase('%Overall')) {
                        ageGroups.each { age ->
                            categories.each { cat ->
                                if (counts[cat] && counts[cat][age]) {
                                    total += counts[cat][age]
                                }
                            }
                        }
                    }

                    def ages
                    if (it.groupBy.equalsIgnoreCase('answer') && it.subset) {
                        ages = getSubsetList(it.subset)
                    } else {
                        ages = ageGroups
                    }

                    def cats
                    if (it.groupBy.equalsIgnoreCase('ageGroup') && it.subset) {
                        cats = getSubsetList(it.subset)
                    } else {
                        cats = categories
                    }

                    def totalForAgeGroup = { ageGroup ->
                        def sum = 0
                        categories.each { cat ->
                            if (counts[cat] && counts[cat][ageGroup]) {
                                sum += counts[cat][ageGroup]
                            }
                        }
                        return sum
                    }
                    
                    if (it.groupBy.equalsIgnoreCase('ageGroup/importance')) {
                        def totals = [:]
                        ages.each { age ->
                            totals[(age)] = totalForAgeGroup(age)
                        }
                        cats.each { cat ->
                            def values = []
                            ages.each { age ->
                                def value = 0
                                if (counts[cat]) {
                                    value = counts[cat][age]
                                }
                                if (it.result.startsWith('%')) {
                                    values << percentage(value, totals[(age)])
                                } else {
                                    values << value
                                }
                            }
                            series << [getImportanceScale()[(cat)], values]
                        }
                        // Get number of respondents in each age group
                        Map ageGroupTotals = Answer.getCountsByValue([ageGroupQuestion.guid], year, false)
                        def values = []
                        ages.each { age ->
                            values << (ageGroupTotals[(age)]?: 0) - totals[(age)]
                        }
                        series << ['Not Important', values]
                        categories = ageGroups
                    } else {
                        ages.each { age ->
                            if (it.result.equalsIgnoreCase('%AgeGroup')) {
                                total = totalForAgeGroup(age)
                            }
    
                            def values = []
                            if (it.groupBy.equalsIgnoreCase('ageGroup')) {
                                def value = 0
                                cats.each { cat ->
                                    if (counts[cat]) {
                                        value += counts[cat][age]
                                    }
                                }
                                if (it.result.startsWith('%')) {
                                    values << percentage(value, total)
                                } else {
                                    values << value
                                }
                            } else {
                                cats.each { cat ->
                                    def value
                                    if (counts[cat]) {
                                        value = counts[cat][age]
                                    } else {
                                        value = 0
                                    }
    
                                    if (it.result.startsWith('%')) {
                                        values << percentage(value, total)
                                    } else {
                                        values << value
                                    }
                                }
                            }
                            String name = ''
                            if (it.groupBy.equalsIgnoreCase('answer')) {
                                name = age
                            } else if (it.groupBy.equalsIgnoreCase('ageGroup')) {
                                name = it.subset
                            }
                            series << [translateLabels(name), values]
                        }
                    }

                    if (it.groupBy.equalsIgnoreCase('ageGroup')) {
                        categories = ageGroups
                        def name = series[0][0]
                        def values = []
                        series.each {
                            values << it[1][0]
                        }
                        series = [[name, values]]
                    }
                    chart['data'] = ['categories': categories.collect { translateLabels(it as String) }, 'series': series]
                    break

                case 'countByAnswerByEmploymentStatus':
                    List guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    List employmentStatusGuids = getEmploymentStatusGuids(qset)
                    guids += employmentStatusGuids
                    Map answers = Answer.getAllAnswers(guids, year, it.answer == 'qtext')

                    def counts = new TreeMap()

                    def incrementCount = { map, cat, emp ->
                        if (!map[cat]) {
                            def cats = [:]
                            getEmploymentCategories().each { ec ->
                                cats[ec] = 0
                            }
                            map[cat] = cats
                        }
                        map[cat][getEmploymentCategory(emp)]++
                    }

                    answers.each { user ->
                        def empStatus = [:]
                        def answer = null
                        user.value.each { guid, value ->
                            if (employmentStatusGuids.contains(guid)) {
                                empStatus[guid] = value['answer']
                            } else {
                                if (value['answer']) {
                                    answer = it.answer.equals('qtext') ? value['qtext'] : value['answer']
                                }
                            }
                        }
                        if (answer) {
                            def emp = getMaxEmploymentStatus(empStatus)
                            if (it.filter) {
                                if (it.filter.equalsIgnoreCase('notHonorary') && !emp.startsWith('Associate')) {
                                    incrementCount(counts, answer, emp)
                                } else if (it.filter.equalsIgnoreCase('fullTimePermanent') && emp.startsWith('Full-time (permanent')) {
                                    incrementCount(counts, answer, emp)
                                }
                            } else {
                                incrementCount(counts, answer, emp)
                            }
                        }
                    }

                    def data = [:]
                    def categories = []
                    def values = []
                    switch (it.result) {
                        case 'percentage':
                            def total = 0
                            counts.each { key, empMap ->
                                empMap.values().each { value ->
                                    total += value
                                }
                            }
                            if (it.stacked?.equalsIgnoreCase('true')) {
                                def series = [:]
                                data['series'] = []
                                def cats = getEmploymentCategories().reverse()
                                getEmploymentCategories().each { cat ->
                                    series[cat] = []
                                }
                                counts.each { key, empMap ->
                                    categories << initialUpper(key as String)
                                    cats.each { cat ->
                                        series[cat] << percentage(empMap[cat],total)
                                    }
                                }
                                cats.each { cat ->
                                    data['series'] << [cat, series[cat]]
                                }
                            } else {
                                counts.each { key, empMap ->
                                    categories << initialUpper(key as String)
                                    values << percentage(empMap.values().sum(),total)
                                }
                                data['series'] = [[year as String, values]]
                            }
                            break

                        default:
                            counts.each { key, empMap ->
                                categories << initialUpper(key as String)
                                values << empMap.values().sum()
                            }
                            data['series'] = [[year as String, values]]
                            break
                    }
                    data['categories'] = categories
                    chart['data'] = data
                    break

                case 'countByAnswerByGender':
                    List guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    def genderQuestionNo = qset.getKnownQuestionNumber('gender')
                    def genderQuestion = modelLoaderService.loadQuestion(qset.setId, genderQuestionNo as int)
                    def genderQuestionGuid = genderQuestion.guid
                    guids += genderQuestionGuid
                    Map answers = Answer.getAllAnswers(guids, year, it.answer == 'qtext')

                    def counts = new TreeMap()

                    def incrementCount = { map, cat, gender ->
                        if (!map[cat]) {
                            def cats = [:]
                            genderQuestion.adata.each { gc ->
                                cats[gc] = 0
                            }
                            map[cat] = cats
                        }
                        map[cat][gender]++
                    }

                    answers.each { user ->
                        def gender = null
                        def answer = null
                        user.value.each { guid, value ->
                            if (guid == genderQuestionGuid) {
                                gender = value['answer']
                            } else {
                                if (value['answer']) {
                                    answer = it.answer.equals('qtext') ? value['qtext'] : value['answer']
                                }
                            }
                        }
                        if (answer && gender) {
                            incrementCount(counts, answer, gender)
                        }
                    }

                    def data = [:]
                    def categories = []
                    switch (it.result) {
                        default:
                            def series = [:]
                            data['series'] = []
                            genderQuestion.adata.each { gc ->
                                series[gc] = []
                            }
                            counts.each { key, genderMap ->
                                categories << initialUpper(key as String)
                                genderQuestion.adata.each { gc ->
                                    series[gc] << genderMap[gc]
                                }
                            }
                            genderQuestion.adata.each { gc ->
                                data['series'] << [gc, series[gc]]
                            }
                            break
                    }
                    data['categories'] = categories
                    chart['data'] = data
                    break

                case 'sumByAnswer':
                    def guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    List totals = Answer.getAnswerTotalsByUser(guids, year, it.answer == 'qtext')

                    def data = [:]
                    def categories = []
                    def values = []

                    if (it.groupBy) {
                        Map options = getGroupByOptions(it.groupBy)
                        switch (options['type']) {
                            case 'decile':
                                def range = options['range'] as int
                                def unit = options['unit']
                                def total = totals.size()
                                List deciles = []
                                for (def i = 0; i < range; i++) { deciles << 0 }
                                if (it.result && it.result == 'percentageInDecile') {
                                    totals.each {
                                        int decile = min(((it - 1)/10) as int, range - 1)
                                        deciles[decile]++
                                    }
                                } else {
                                    // count occurrences of % in corresponding decile
                                    totals.each {
                                        deciles[((it - 1)/10) as int]++
                                    }
                                }

                                deciles.eachWithIndex { it2, i ->
                                    def decile
                                    if (i == 0) {
                                        decile = '0-10 ' + unit
                                    } else if (i == range - 1 && range < 10) {
                                        decile = ">${(range - 1) * 10} ${unit}"
                                    } else {
                                        decile = "${i * 10 + 1}-${(i + 1) * 10} ${unit}"
                                    }
                                    categories << decile
                                    def value = it2
                                    if (it.result && it.result == 'percentageInDecile') {
                                        value = percentage(value, total)
                                    }
                                    values << value
                                }
                                data['categories'] = categories
                                data['series'] = [[year as String, values]]
                                chart['data'] = data
                                break
                        }
                    }
                    break

                case 'sumByAnswerByEmploymentStatus':
                    def guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    def employmentStatusGuids = getEmploymentStatusGuids(qset)
                    List answers = Answer.getAnswerTotalsByUserByEmploymentStatus(guids, employmentStatusGuids, year, it.answer == 'qtext')

                    def data = [:]
                    def categories = []
                    def values = []

                    if (it.groupBy) {
                        Map options = getGroupByOptions(it.groupBy)
                        switch (options['type']) {
                            case 'employment':

                                def totals = [:]
                                categories = getEmploymentCategories()
                                categories.each { cat ->
                                    totals[cat] = 0
                                }

                                answers.each { ans ->
                                    def empCat = getEmploymentCategory(getMaxEmploymentStatus(ans[1]))
                                    totals[empCat] += ans[0]
                                }

                                categories.each { cat ->
                                    values << totals[cat]
                                }
                                
                                data['categories'] = categories
                                data['series'] = [[year as String, values]]
                                chart['data'] = data
                                break
                        }
                    }
                    break

                case 'countByAnswerByGuid':
                    def guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    Map answers = Answer.getAllAnswers(guids, year, it.answer == 'qtext')
                    def noOfUsers = answers.size()
                    def answerValues = q.qdata.cols.clone()

                    // init counts of each answer for each guid
                    def counts = [:]
                    if (it.allCategories == 'true') {
                        guids.each { guid ->
                            def answerTotals = [:]
                            answerValues.each { answerTotals[it] = 0 }
                            counts[guid] = answerTotals
                        }
                    }

                    answers.each { answer ->
                        answer.value.each { guid, value ->
                            if (value['answer']) {
                                counts[guid][value['answer']]++
                            }
                        }
                    }

                    def categories = []
                    counts.each { key, value ->
                        def question = Question.findByGuid(key as String)
                        categories << translateLabels(question.qtext)
                    }

                    def series = []
                    answerValues.each { answer ->
                        def name = answer
                        def values = []
                        counts.each { key, value ->
                            if (it.result.equalsIgnoreCase('percentage')) {
                                values << percentage(value[answer], noOfUsers)
                            } else {
                                values << value[answer]
                            }
                        }
                        series << [name, values]
                    }

                    def data = [:]
                    data['categories'] = categories
                    data['series'] = series
                    chart['data'] = data
                    break

                case 'sumByAnswerByGuid':
                    def guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    Map totals = Answer.getAnswerTotalsByGuid(guids, year, it.answer == 'qtext')

                    def pairs = []
                    if (!it.answer) {
                        totals.each { key, value ->
                            def question = Question.findByGuid(key as String)
                            def qtext
                            if (question.shorttext) {
                                qtext = question.shorttext
                            } else if (question.qtext) {
                                qtext = question.qtext
                            } else {
                                def json = JSON.parse(question.adata)
                                qtext = json.row
                            }
                            pairs << [qtext, value]
                        }
                    }

                    if (it.groupBy?.equalsIgnoreCase('employment')) {
                        def group = [:]
                        getEmploymentCategories().each { cat ->
                            group[cat] = 0
                        }
                        pairs.each { key, value ->
                            group[getEmploymentCategory(key)] += value
                        }
                        pairs = []
                        group.each { key, value ->
                            pairs << [key, value]
                        }
                    }

                    if (it.order == 'category') {
                        pairs = pairs.sort(ascKeyComp)
                    } else {
                        pairs = pairs.sort(descValueComp)
                    }

                    def categories = []
                    def values = []
                    pairs.each { key, value ->
                        categories << key
                        values << value
                    }
                    def data = [:]
                    data['categories'] = categories
                    data['series'] = [[year as String, values]]
                    chart['data'] = data
                    break

                case 'averageByAnswer':
                    def guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    Map answers = Answer.getAllAnswers(guids, year, it.answer == 'qtext')
                    def noOfUsers = answers.size()

                    // total up values grouped by guid
                    def totals = [:]
                    answers.each { answer ->
                        answer.value.each { guid, value ->
                            if (value['answer']) {
                                def answerVal = value['qtext'] ?: value['answer']
                                if (totals[guid]) {
                                    totals[guid] += answerVal as int
                                } else {
                                    totals[guid] = answerVal as int
                                }
                            }
                        }
                    }

                    def pairs = []
                    totals.each { key, value ->
                        def question = Question.findByGuid(key as String)
                        def average = String.format('%.1f', value/noOfUsers)
                        pairs << [question.aggregationText, average]
                    }
                    pairs = pairs.sort(descValueComp)
                    def categories = []
                    def values = []
                    pairs.each { key, value ->
                        categories << key
                        values << value
                    }
                    def data = [:]
                    data['categories'] = categories
                    data['series'] = [[year as String, values]]
                    chart['data'] = data
                    break

                case 'countByCategory':
                    def guids = it.subLevel ? getQuestionGuids(q, it.subLevel) : [q.guid]
                    Map answers = Answer.getAllAnswers(guids, year, it.answer == 'qtext')
                    def noOfUsers = answers.size()

                    // init counts for each guid
                    def counts = [:]
                    if (it.allCategories == 'true') {
                        guids.each {
                            counts[it] = 0
                        }
                    }

                    def incrementCount = { count, key ->
                        if (count[key]) {
                            count[key]++
                        } else {
                            count[key] = 1
                        }
                    }

                    answers.each { answer ->
                        answer.value.each { guid, value ->
                            if (value['answer']) {
                                if (it.value) {
                                    if (it.value.equals(value['answer'])) {
                                        incrementCount(counts, guid)
                                    }
                                } else {
                                    incrementCount(counts, guid)
                                }
                            }
                        }
                    }

                    def pairs = []
                    counts.each { key, value ->
                        def question = Question.findByGuid(key as String)
                        def label = question.aggregationText ?: question.qtext
                        if (it.result == 'percentage') {
                            pairs << [label, percentage(value, noOfUsers)]
                        } else {
                            pairs << [label, value]
                        }
                    }
                    pairs = pairs.sort(descValueComp)
                    def categories = []
                    def values = []
                    pairs.each { key, value ->
                        categories << key
                        values << value
                    }
                    def data = [:]
                    data['categories'] = categories
                    data['series'] = [[year as String, values]]
                    chart['data'] = data
                    break

                default:
                    break
            }
            chartData << chart
        }

        return chartData

    }

    Map getImportanceScale() {
        return new TreeMap(
            [ '1' : 'Most important',
              '2' : 'Important',
              '3' : 'Somewhat important',
              '4' : 'Less important',
              '5' : 'Least important' ])
    }

    private List getQuestionGuids(QuestionModel q, String subLevel) {
        List guids = []
        def tokens = subLevel.tokenize('/=')
        def level = tokens[0]
        def qtext
        def col
        def type
        def range = []
        if (tokens.size() > 1) {
            switch (tokens[1]) {
                case 'qtext':
                    qtext = tokens[2]
                    break
                case 'col':
                    col = tokens[2]
                    break
                case 'type':
                    type = tokens[2]
                    break
                case 'range':
                    def rangeLimits = tokens[2].tokenize('..')
                    for (int i = rangeLimits[0] as int; i <= (rangeLimits[1] as int); i++) {
                        range << i - 1
                    }
                    break
            }
        }

        def getGuids = { list, question ->
            if (qtext) {
                if (question.qtext == qtext) {
                    list << question.guid
                }
            } else if (col) {
                if (question.owner.qdata.cols[(col as int) - 1] == question.adata.col) {
                    if (isChartable(question)) {
                        list << question.guid
                    }
                }
            } else if (type) {
                if (question.atype == AnswerType.valueOf(type)) {
                    list << question.guid
                }
            } else {
                if (isChartable(question)) {
                    list << question.guid
                }
            }
        }

        switch (level) {
            case '1':
                q.questions.each { it ->
                    getGuids(guids, it)
                }
                break

            case '2':
                q.questions.eachWithIndex { it, i ->
                    if (range) {
                        if (i in range) {
                            it.questions.each { it2 ->
                                getGuids(guids, it2)
                            }
                        }
                    } else {
                        it.questions.each { it2 ->
                            getGuids(guids, it2)
                        }
                    }
                }
                break

            case 'all':
                q.questions.each { it ->
                    getGuids(guids, it)
                    it.questions.each { it2 ->
                        getGuids(guids, it2)
                    }
                }
                break
        }

        return guids
    }

    private Map getGroupByOptions(String groupBy) {
        Map options = [:]
        def tokens = groupBy.tokenize('/')
        options['type'] = tokens[0]
        options['range'] = tokens[1]
        options['unit'] = tokens[2]
        return options
    }

    private String percentage(def value, def total) {
        if (total == 0) {
            return '0.0'
        } else {
            def percent = value/total * 100.0
            return String.format('%.1f', percent)
        }
    }

    private String initialUpper(String str) {
        return str.substring(0,1).toUpperCase() + str.substring(1)
    }

    private int min(int a, int b) {
        if (a < b) {
            return a
        } else {
            return b
        }
    }

    private boolean isChartable(QuestionModel q) {
        if (q.atype == AnswerType.summable) {
            def totalRowIndex = q.owner.adata.sumRow as int
            def totalRowLabel = q.owner.qdata.rows[totalRowIndex - 1]
            return totalRowLabel != q.adata.row
        } else return q.atype != AnswerType.calculate
    }

    private List getRangeLabels(QuestionModel q) {
        def labels = []
        for (int i = q.adata.start; i < q.adata.end; i += q.adata.interval) {
            labels << "${i}-${i + q.adata.interval - 1}"
        }
        if (q.adata.over) {
            labels << "${q.adata.end + 1} ${q.adata.over}"
        }
        if (q.adata.alt) {
            labels << q.adata.alt
        }
        return labels
    }

    private String translateLabels(String s) {
        switch (s) {
            case 'It may be likely': return 'May be likely'
            case 'Unsure/haven\'t decided': return 'Unsure'
            case '60 and over': return '60+'
            case 'Other (please specify)': return 'Other'
            case 'The overall image of taxonomy': return 'Overall image of taxonomy'
            case 'The need to strengthen inter-institutional links': return 'Need to strengthen inter-institutional links'
            default: return s
        }
    }

    private boolean inSubset(String category, String ageGroup, String subset) {
        if (subset) {
            def allowed = getSubsetList(subset)
            return allowed.contains(category) || allowed.contains(ageGroup)
        } else {
            return true
        }
    }

    private List getSubsetList(String subset) {
        return subset.tokenize('|')
    }

    private List getEmploymentCategories() {
        return ['Full-time', 'Part-time', 'Associate/Volunteer', 'Student/Other']
    }

    private String getEmploymentCategory(String status) {
        def cats = getEmploymentCategories()
        switch (status) {
            case ~/^Full-time.*/: return cats[0]
            case ~/^Part-time.*/: return cats[1]
            case ~/^Associate.*/: return cats[2]
            case ['Student', 'Other']: return cats[3]
        }
    }

    private List getEmploymentStatusGuids(QuestionSet qset) {
        def employmentStatusQuestionNo = qset.getKnownQuestionNumber('employmentStatus')
        def employmentStatusQuestion = modelLoaderService.loadQuestion(qset.setId, employmentStatusQuestionNo as int)
        return getQuestionGuids(employmentStatusQuestion, '2/type=number')
    }

    private String getMaxEmploymentStatus(Map workingHours) {
        def maxCat = workingHours.entrySet().max{ it.value as int}
        def question = Question.findByGuid(maxCat.key)
        return question.qtext
    }
}
