cd ../

call mvn clean package -T 8C

IF NOT %ERRORLEVEL% == 0 (
    exit /b %ERRORLEVEL%
)

git fetch

git pull

git checkout main

git merge origin/develop

git checkout develop

ssh root@15.229.18.114 "pm2 delete api.finax"

scp -r ./target/finax.jar root@15.229.18.114:/home/ubuntu/workspace/finax_api/

ssh root@15.229.18.114 "/root/start_finax_api.sh"