pipeline{
    agent any
    environment{
        DOCKER_REPOSITORY="riteshsoni296"
        SERVICE_EXPOSE_TYPE="LoadBalancer"
        KUBERNETES_CLUSTER_IP="192.168.99.106"
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
                    try{
                        sh (label:'Deployment_Name', script: 'kubectl get deployment $CODE_BASE -n $JOB_NAME')
                        env.DEPLOYMENT_STATUS_CODE= 0
                    }catch(e){
                        println("Application is not yet Deployed")
                        env.DEPLOYMENT_STATUS_CODE = 1
                    }  
                    if (DEPLOYMENT_STATUS_CODE != "1"){
                        def container_name = sh( label:"Container_Name", script: 'kubectl get deploy $CODE_BASE -n $JOB_NAME -o jsonpath="{.spec.template.spec.containers[*].name}"', returnStdout: true)
                        //Rollout of new application
                        try{
                            sh ( label:"Rollout_App", script: "kubectl set image deployment/$CODE_BASE -n $JOB_NAME $container_name=$DOCKER_REPOSITORY/$CODE_BASE:$IMAGE_VERSION", returnStdout: true)
                                
                            //Wait for the rollout to be complete
                            sh( label:"Rollout Status", script: "kubectl rollout status deploy/$CODE_BASE -n $JOB_NAME | grep success", returnStdout: true)
                        }catch(e){
                            error("Rollout of new Application Failed")
                        }
                    }
                }     
            }
        }
        stage("Application Deployment"){
            when{
                environment name: 'DEPLOYMENT_STATUS_CODE', value: '1'
            }
            options {
                timeout(time: 5, unit: 'MINUTES') 
            }
            steps{
                script{
                    def new_deploy_status_code = sh( label:"New Deployment", script: 'kubectl create deployment $CODE_BASE -n $JOB_NAME --image $DOCKER_REPOSITORY/$CODE_BASE:$IMAGE_VERSION', returnStatus: true)
	
                    if ( new_deploy_status_code == 0 ){            
                        //Wait till the pods are in running state
                        while (true)
                        {
                            def pods_status_code = sh( label: "Pods Status", script: 'kubectl get pods -n $JOB_NAME -l app=$CODE_BASE -o jsonpath="{.items[*].status.containerStatuses[*].state.running}"', returnStdout: true) 
                            if (pods_status_code){
                                sleep 5
                            }
                            else{
                                break
                            }
                        }
                        //Expose the application using service
                        sh( label:"Service Resource", script: 'kubectl expose deployment/$CODE_BASE -n $JOB_NAME --port 80 --type=$SERVICE_EXPOSE_TYPE')
                    }
                    else{
                        error("Failed to create a deployment")
                    }
                }
            }
        }
        stage("Application Testing"){
            steps{
                sh( label:"Application Service", script: "kubectl get svc $CODE_BASE -n $JOB_NAME")
                script{
                    def application_port = sh(label:"Application Port", script: "kubectl get svc $CODE_BASE -n $JOB_NAME -o jsonpath=\"{.spec.ports[0].nodePort}\"", returnStdout: true)
                    def application_status = sh(label:"Testing Application", script: "curl -s -w \"%{http_code}\" -o /dev/null $KUBERNETES_CLUSTER_IP:$application_port", returnStdout: true)
                    if (application_status == "200"){
                        println("Application is working Fine")
                    }    
                    else{
                        error("Application Not Working Properly ")
                    }
                } 
            }
            post{
                success{
                    script{
                        if ( GIT_BRANCH == "origin/develop" ){
                            println("Merging  Code to Production Code Base")
                            sh 'git fetch --all'
                            sh 'git checkout origin/master'
                            sh 'git merge origin/develop'
                            //## Pushing code to master
                            withCredentials([usernamePassword(credentialsId: 'GITHUB_CREDENTIALS', passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                                sh('git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/riteshsoni10/demo_website.git HEAD:master')
                            }
                        }
                        else{
                            println("Aplication Tested Successfully")
                        }
                    }
                }
                failure{
                    println("Build Failed")
                    emailext attachLog: true,
                        body: "${currentBuild.currentResult}: Job ${JOB_NAME} build ${BUILD_NUMBER}\n More info at: ${BUILD_URL}",
                        compressLog: true,
                        recipientProviders: [developers(), requestor()],
                        subject: "Jenkins Build ${currentBuild.currentResult}: Job ${JOB_NAME}"
                }
            }
        }
    }
}



