/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.gxlx.mr.spark;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Tuple2;

public final class JavaWordCountExample {
    static Logger logger = LoggerFactory.getLogger(JavaWordCountExample.class);
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) throws Exception {
        if (args.length < 0) {
            System.err.println("Usage: JavaWordCount <file>");
            System.exit(1);
        }

        logger.info("ready go");

        SparkConf sparkConf = new SparkConf().setAppName("JavaWordCountExample");

        for (Tuple2<?, ?> tuple : sparkConf.getAll()) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }
        sparkConf.setMaster("local[3]");
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith("win")) {
            System.setProperty("hadoop.home.dir", "D:\\workplugin\\hadoop\\hadoop26");
        }

        System.out.println("HADOOP_CONF_DIR=" + System.getenv("HADOOP_CONF_DIR"));

        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        JavaRDD<String> lines = ctx.textFile("hdfs://master:9000/hdfs-site.xml", 1);

        logger.info("-------------------lines==" + lines);

        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String s) {
                return Arrays.asList(SPACE.split(s));
            }
        });

        logger.info("-------------------words==" + words);

        JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<String, Integer>(s, 1);
            }
        });
        logger.info("-------------------ones==" + ones);
        JavaPairRDD<String, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });
        logger.info("-------------------counts==" + counts);
        List<Tuple2<String, Integer>> output = counts.collect();

        logger.info("-------------------output==" + output);
        for (Tuple2<?, ?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }

        ctx.stop();
    }
}
