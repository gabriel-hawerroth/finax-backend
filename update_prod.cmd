call mvn clean package

ssh root@15.229.18.114 "pm2 delete api.finax"

ssh root@15.229.18.114 "rm -rf /home/ubuntu/workspace/finax_api/*"

scp -r D:\finax\Finax-backend\target\finax.jar root@15.229.18.114:/home/ubuntu/workspace/finax_api/

ssh root@15.229.18.114 "/root/start_finax_api.sh"