cd ../

call mvn clean package -T 2C

IF NOT %ERRORLEVEL% == 0 (
    exit /b %ERRORLEVEL%
)

ssh root@15.229.18.114 "pm2 delete api.finax"
ssh root@15.229.18.114 "rm -rf /home/ubuntu/workspace/finax_api/finax.jar"

scp -r ./target/finax.jar root@15.229.18.114:/home/ubuntu/workspace/finax_api/

ssh root@15.229.18.114 "/root/start_finax_api.sh"
