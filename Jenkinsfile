pipeline{
    agent any
    environment{
        DOCKER_REPOSITORY="riteshsoni296"
    }
    stages{
        stage('Detect Code Base'){
            steps{
                script{
                    if [ $(find . -type f \( -name "*.php" -a -name "*.html" \) | wc -l ) -gt 0 ]
                    then
	                    echo "PHP and HTML Code found"
                        DEPLOYMENT_NAME="php-application"
                    ## Checking if the only HTML language code is present
                    elif [ $(find . -type f  -name "*.html" | wc -l ) -gt 0 ]; then
	                    echo "HTML Code found"
                        DEPLOYMENT_NAME="web-application"
                    fi
                }
            }
        }
        stage("Build Image"){
            steps{
                script{
                    if ( $GIT_BRANCH == "master" )
                    then
                        docker build . -t $DOCKER_REPOSITORY/$DEPLOYMENT_NAME'_release':v${BUILD_NUMBER} --no-cache

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

