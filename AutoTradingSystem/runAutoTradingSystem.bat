rem mvn clean package -Dmaven.test.skip=true
cd C:\Users\CzeHoul\workspace\AutoTradingSystem
mvn exec:java -Duser.timezone=GMT+8 > C:\automatedTrading\AutoTradingSystem\Log\runAutoTradingSystem_%DATE:~-4%-%DATE:~7,2%-%DATE:~4,2%.out 2>&1