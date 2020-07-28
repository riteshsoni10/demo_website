def repo_url=getBinding().getVariables()['GIT_URL']

pipelineJob('test-env'){
    description('Web Application Deployment in Test Environment using Jenkins Pipeline')
    properties {
        githubProjectUrl("$repo_url")
    }
    definition {
        triggers {
             scm(* * * * *)
        }
       cpsScm {
            scm {
                git{
                    remote {
                        url("$repo_url")
                    }
                    branch('*/develop')     
                }
            }
            lightweight()
        }
    }
}

pipelineJob('prod-env'){
    description('Web Application Deployment in Production Environment using Jenkins Pipeline')
    properties {
        githubProjectUrl("$repo_url")
    }
    definition {
        triggers {
            //githubPush()
            scm(* * * * *)
        }
       cpsScm {
            scm {
                git{
                    remote {
                        url("$repo_url")
                    }
                    branch('*/master')     
                }
            }
            lightweight()
        }
    }
}


