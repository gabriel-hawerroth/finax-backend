#!/bin/bash

cd ..

# Executa o build usando Maven
mvn clean package

# Verifica se o build foi bem-sucedido
if [ $? -ne 0 ]; then
    echo "O build falhou. Parando a execução do script."
    exit 1
fi

# Deleta o processo existente do PM2 no servidor remoto
ssh root@15.229.18.114 "pm2 delete api.finax"

# Substitui arquivo JAR para o servidor remoto
scp -r ./target/finax.jar root@15.229.18.114:/home/ubuntu/workspace/finax_api/

# Inicia o processo no servidor remoto
ssh root@15.229.18.114 "/root/start_finax_api.sh"
