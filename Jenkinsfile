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
                        env.IMAGE_VERSION="RELEASE_v${BUILD_NUMBER}"
                        sh 'docker build . -t ${DOCKER_REPOSITORY}/${CODE_BASE}:${IMAGE_VERSION} --no-cache'
                    }
                    else if ( GIT_BRANCH == "origin/develop" ){
                        env.IMAGE_VERSION="TEST_v${BUILD_NUMBER}"
                        sh 'docker build . -t ${DOCKER_REPOSITORY}/${CODE_BASE}:${IMAGE_VERSION} --no-cache'
                    }
                    else{
                        error("Please push code to develop BRANCH for testing ")
                    }
                }
            }
        }
        stage("Image Repo Login"){
            steps{
                withCredentials([usernamePassword(credentialsId: 'DOCKER_HUB_CREDENTIALS', passwordVariable: 'docker_password', usernameVariable: 'docker_user')]) {
                    sh 'docker login -u="$docker_user" -p="$docker_password"'
                }
            }
        }
        stage("Push Image"){
            steps{
                sh 'docker push $DOCKER_REPOSITORY/$CODE_BASE:$IMAGE_VERSION'
            }
        }
        stage("Application Namespace"){
            steps{
                script{
                    def status_code = sh (label: 'Namespace', script: 'kubectl get ns $JOB_NAME >/dev/null', returnStatus: true )
                    if ( status_code != 0 )
                    {
                        sh 'kubectl create ns $JOB_NAME'
                    }
                }
            }
        }
        stage("Application Rollout"){
            steps{
                script{
                    env.deployment_status_code = sh (label:'Deployment_Name', script: 'kubectl get deployment $CODE_BASE -n ${JOB_NAME}', returnStatus: true, returnStdout: false)
                    if ( deployment_status_code == 0 ){
                        def container_name = sh( label:"Container_Name", script: 'kubectl get deploy ${CODE_BASE} -n ${JOB_NAME} -o jsonpath="{.spec.template.spec.containers[*].name}"', returnStdout: true)
                        
                        #Rollout of new application
                        sh ( label:"Rollout_App", script: 'kubectl set image deployment/${CODE_BASE} -n ${JOB_NAME} ${container_name}=${DOCKER_REPOSITORY}/${CODE_BASE}:${IMAGE_VERSION}')
                        
                        # Wait for the rollout to be complete
                        rollout_status_code =  sh( label:"Rollout Status", script: 'kubectl rollout status deploy/$CODE_BASE -n $JOB_NAME | grep success', returnStatus: true)
                        if ( rollout_status_code != 0 ){
                            error("Rollout of new Application Failed")
                        }    
                    }
                }
            }
        }

    }
}

