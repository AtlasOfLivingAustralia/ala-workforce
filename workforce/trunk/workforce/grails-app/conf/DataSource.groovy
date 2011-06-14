dataSource {
    pooled = true
    driverClassName = "com.mysql.jdbc.Driver"
    username = "wfadmin"
    password = "monga"
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = true
    cache.provider_class = 'net.sf.ehcache.hibernate.EhCacheProvider'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "update" // one of 'create', 'create-drop','update'
            url = "jdbc:mysql://localhost:3306/workforce?autoreconnect=true"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/workforce?autoreconnect=true"
        }
    }
    testserver {
        dataSource {
            dbCreate = "update"
            //url = "jdbc:mysql://ala-testweb1.vm.csiro.au:3306/workforce?autoReconnect=true&connectTimeout=0"
            url = "jdbc:mysql://ala-testdb1.vm.csiro.au:3306/workforce?autoReconnect=true&connectTimeout=0"
            //url = "jdbc:mysql://localhost:3306/workforce?autoReconnect=true&connectTimeout=0"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:mysql://localhost:3306/workforce?autoreconnect=true&connectTimeout=0"
        }
    }
}
