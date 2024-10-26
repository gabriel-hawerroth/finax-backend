#!/bin/bash

cd ..

mvn clean package -T 6C

# Verifica se o build foi bem-sucedido
if [ $? -ne 0 ]; then
    echo "O build falhou. Parando a execução do script."
    exit 1
fi

git fetch origin

git checkout develop
git pull origin develop

git checkout main
git pull origin main

git merge origin/develop --no-edit
git push origin main

git checkout develop

ssh ubuntu@server3 "pm2 delete api.finax"

scp -r ./target/finax.jar ubuntu@server3:/home/ubuntu/finax_builds/back/

ssh ubuntu@server3 "/home/ubuntu/start_scripts/start_finax_api.sh"
