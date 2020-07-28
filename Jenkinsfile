pipeline{
    agent any
    environment{
        DOCKER_REPOSITORY="riteshsoni296"
    }
    stages{
        stage('Detect Code Base'){
            steps{
                script{
                    def files_list = findFiles(glob: "*.php")
                    if ( files_list == [] ){
                       files_list = findFiles(glob: "*.html")
                       if ( files_list == [] ){
                            println("Unknown Code Base Detected")
                            error("Unknown Code Base Detected")
                       }
                       else{
                           println("HTML Code Base Detected")
                           env.CODE_BASE="web-application"
                           sh 'printenv'
                       }
                    }
                    else{
                         println("PHP Code Base Detected")
                         env.CODE_BASE="php-application"
                    }    
                }
            }
        }
        stage("Build Image"){
            steps{
                script{
                    if ( $GIT_BRANCH == "master" ){
                        sh 'docker build . -t ${DOCKER_REPOSITORY}/${CODE_BASE}_release:v${BUILD_NUMBER} --no-cache'
                    }
                }
            }
        }
        stage("Push Image"){
            steps{
               script{
                   echo "hello"
               }
            }
        }
    }
}

