spark-submit  \
    --master yarn \
    --deploy-mode cluster \
    --queue bigdataoffline  \
    --executor-memory 2560M \
    --executor-cores 2 \
    --driver-memory 2560M \
    --driver-cores 2 \
    --jars hdfs:///bigdata/spark_lib/ojdbc6.jar,hdfs:///bigdata/spark_lib/mysql-connector-java-8.0.19.jar \
    --conf spark.shuffle.service.enabled=true \
    --conf spark.dynamicAllocation.enabled=true \
    --conf spark.dynamicAllocation.executorIdleTimeout=30s \
    --conf spark.dynamicAllocation.initialExecutors=4 \
    --conf spark.dynamicAllocation.maxExecutors=20 \
    --conf spark.dynamicAllocation.minExecutors=4    \
    --class com.hqyg.bigdata.spoop.Spoop \
    stg2ods-1.0-elog.jar \
    -m import -job testJob -db_num 103_01 -tables S_OMS_USERS -date 20200423