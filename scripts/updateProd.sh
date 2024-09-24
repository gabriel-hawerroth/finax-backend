#!/bin/bash

cd ..

mvn clean package -T 8C

# Verifica se o build foi bem-sucedido
if [ $? -ne 0 ]; then
    echo "O build falhou. Parando a execução do script."
    exit 1
fi

git fetch origin

git checkout develop
git pull origin develop

git checkout main
git merge origin/develop
git push origin main
git checkout develop

ssh root@15.229.18.114 "pm2 delete api.finax"

# Substitui arquivo JAR para o servidor remoto
scp -r ./target/finax.jar root@15.229.18.114:/home/ubuntu/workspace/finax_api/

ssh root@15.229.18.114 "/root/start_finax_api.sh"
