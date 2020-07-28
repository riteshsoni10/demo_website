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
                    if ( GIT_BRANCH == "origin/master" ){
                        sh 'docker build . -t ${DOCKER_REPOSITORY}/${CODE_BASE}:RELEASE_v${BUILD_NUMBER} --no-cache'
                    }
                    else if ( GIT_BRANCH == "origin/develop" ){
                        sh 'docker build . -t ${DOCKER_REPOSITORY}/${CODE_BASE}:TEST_v${BUILD_NUMBER} --no-cache'
                    }
                    else{
                        error("Please push code to develop BRANCH for testing ")
                    }
                }
            }
        }
        stage("Docker Hub Login"){
            steps{
                withCredentials([usernamePassword(credentialsId: 'DOCKER_HUB_CREDENTIALS', passwordVariable: 'docker_password', usernameVariable: 'docker_userr')]) {
                    sh 'docker login -u="$docker_user" -p="$docker_password"'
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

