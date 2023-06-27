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

package org.apache.seatunnel.connectors.seatunnel.jdbc;

import org.apache.seatunnel.api.table.type.SeaTunnelRow;
import org.apache.seatunnel.common.exception.SeaTunnelRuntimeException;

import org.apache.commons.lang3.tuple.Pair;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerLoggerFactory;

import com.google.common.collect.Lists;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcOceanBasedbIT extends AbstractJdbcIT {

    private static final String OCEANBASE_IMAGE = "oceanbase/oceanbase-ce:4.0.0.0";

    private static final String OCEANBASE_CONTAINER_HOST = "e2e_oceanbaseDb";
    private static final String DRIVER_CLASS = "com.oceanbase.jdbc.Driver";
    private static final int PORT = 2881;
    private static final String DATABASE = "seatunnel";
    private static final String URL =
            "jdbc:oceanbase://"
                    + HOST
                    + ":"
                    + PORT
                    + "/"
                    + DATABASE
                    + "?createDatabaseIfNotExist=true&_enable_convert_real_to_decimal=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    private static final String SOURCE_TABLE = "e2e_ob_source";
    private static final String SINK_TABLE = "e2e_ob_sink";
    private static final String DRIVER_JAR =
            "https://repo1.maven.org/maven2/com/oceanbase/oceanbase-client/2.4.3/oceanbase-client-2.4.3.jar";
    private static final List<String> CONFIG_FILE =
            Lists.newArrayList("/jdbc_oceanbase_source_and_sink.conf");

    private static final String CREATE_SQL =
            "CREATE TABLE IF NOT EXISTS %s\n"
                    + "(\n"
                    + "    `c_bit_1`                bit(1)                DEFAULT NULL,\n"
                    + "    `c_bit_8`                bit(8)                DEFAULT NULL,\n"
                    + "    `c_bit_16`               bit(16)               DEFAULT NULL,\n"
                    + "    `c_bit_32`               bit(32)               DEFAULT NULL,\n"
                    + "    `c_bit_64`               bit(64)               DEFAULT NULL,\n"
                    + "    `c_boolean`              tinyint(1)            DEFAULT NULL,\n"
                    + "    `c_tinyint`              tinyint(4)            DEFAULT NULL,\n"
                    + "    `c_tinyint_unsigned`     tinyint(3) unsigned   DEFAULT NULL,\n"
                    + "    `c_smallint`             smallint(6)           DEFAULT NULL,\n"
                    + "    `c_smallint_unsigned`    smallint(5) unsigned  DEFAULT NULL,\n"
                    + "    `c_mediumint`            mediumint(9)          DEFAULT NULL,\n"
                    + "    `c_mediumint_unsigned`   mediumint(8) unsigned DEFAULT NULL,\n"
                    + "    `c_int`                  int(11)               DEFAULT NULL,\n"
                    + "    `c_integer`              int(11)               DEFAULT NULL,\n"
                    + "    `c_bigint`               bigint(20)            DEFAULT NULL,\n"
                    + "    `c_bigint_unsigned`      bigint(20) unsigned   DEFAULT NULL,\n"
                    + "    `c_decimal`              decimal(20, 0)        DEFAULT NULL,\n"
                    + "    `c_decimal_unsigned`     decimal(38, 18)       DEFAULT NULL,\n"
                    + "    `c_float`                float                 DEFAULT NULL,\n"
                    + "    `c_float_unsigned`       float unsigned        DEFAULT NULL,\n"
                    + "    `c_double`               double                DEFAULT NULL,\n"
                    + "    `c_double_unsigned`      double unsigned       DEFAULT NULL,\n"
                    + "    `c_char`                 char(1)               DEFAULT NULL,\n"
                    + "    `c_tinytext`             tinytext,\n"
                    + "    `c_mediumtext`           mediumtext,\n"
                    + "    `c_text`                 text,\n"
                    + "    `c_varchar`              varchar(255)          DEFAULT NULL,\n"
                    + "    `c_json`                 json                  DEFAULT NULL,\n"
                    + "    `c_longtext`             longtext,\n"
                    + "    `c_date`                 date                  DEFAULT NULL,\n"
                    + "    `c_datetime`             datetime              DEFAULT NULL,\n"
                    + "    `c_timestamp`            timestamp NULL        DEFAULT NULL,\n"
                    + "    `c_tinyblob`             tinyblob,\n"
                    + "    `c_mediumblob`           mediumblob,\n"
                    + "    `c_blob`                 blob,\n"
                    + "    `c_longblob`             longblob,\n"
                    + "    `c_varbinary`            varbinary(255)        DEFAULT NULL,\n"
                    + "    `c_binary`               binary(1)             DEFAULT NULL,\n"
                    + "    `c_year`                 year(4)               DEFAULT NULL,\n"
                    + "    `c_int_unsigned`         int(10) unsigned      DEFAULT NULL,\n"
                    + "    `c_integer_unsigned`     int(10) unsigned      DEFAULT NULL,\n"
                    + "    `c_bigint_30`            BIGINT(40)  unsigned  DEFAULT NULL,\n"
                    + "    `c_decimal_unsigned_30`  DECIMAL(30) unsigned  DEFAULT NULL,\n"
                    + "    `c_decimal_30`           DECIMAL(30)           DEFAULT NULL\n"
                    + ");";

    @Override
    JdbcCase getJdbcCase() {
        Map<String, String> containerEnv = new HashMap<>();
        Pair<String[], List<SeaTunnelRow>> testDataSet = initTestData();
        String jdbcUrl = String.format(URL, PORT, DATABASE);
        String[] fieldNames = testDataSet.getKey();
        String insertSql = insertTable(DATABASE, SOURCE_TABLE, fieldNames);

        return JdbcCase.builder()
                .dockerImage(OCEANBASE_IMAGE)
                .networkAliases(OCEANBASE_CONTAINER_HOST)
                .containerEnv(containerEnv)
                .driverClass(DRIVER_CLASS)
                .host(HOST)
                .port(PORT)
                .localPort(PORT)
                .jdbcTemplate(URL)
                .jdbcUrl(jdbcUrl)
                .userName(USERNAME)
                .password(PASSWORD)
                .database(DATABASE)
                .sourceTable(SOURCE_TABLE)
                .sinkTable(SINK_TABLE)
                .createSql(CREATE_SQL)
                .configFile(CONFIG_FILE)
                .insertSql(insertSql)
                .testData(testDataSet)
                .build();
    }

    @Override
    void compareResult() {}

    @Override
    String driverUrl() {
        return DRIVER_JAR;
    }

    @Override
    Pair<String[], List<SeaTunnelRow>> initTestData() {
        String[] fieldNames =
                new String[] {
                    "c_bit_1",
                    "c_bit_8",
                    "c_bit_16",
                    "c_bit_32",
                    "c_bit_64",
                    "c_boolean",
                    "c_tinyint",
                    "c_tinyint_unsigned",
                    "c_smallint",
                    "c_smallint_unsigned",
                    "c_mediumint",
                    "c_mediumint_unsigned",
                    "c_int",
                    "c_integer",
                    "c_year",
                    "c_int_unsigned",
                    "c_integer_unsigned",
                    "c_bigint",
                    "c_bigint_unsigned",
                    "c_decimal",
                    "c_decimal_unsigned",
                    "c_float",
                    "c_float_unsigned",
                    "c_double",
                    "c_double_unsigned",
                    "c_char",
                    "c_tinytext",
                    "c_mediumtext",
                    "c_text",
                    "c_varchar",
                    "c_json",
                    "c_longtext",
                    "c_date",
                    "c_datetime",
                    "c_timestamp",
                    "c_tinyblob",
                    "c_mediumblob",
                    "c_blob",
                    "c_longblob",
                    "c_varbinary",
                    "c_binary",
                    "c_bigint_30",
                    "c_decimal_unsigned_30",
                    "c_decimal_30",
                };

        List<SeaTunnelRow> rows = new ArrayList<>();
        BigDecimal bigintValue = new BigDecimal("2844674407371055000");
        BigDecimal decimalValue = new BigDecimal("999999999999999999999999999899");
        for (int i = 0; i < 100; i++) {
            byte byteArr = Integer.valueOf(i).byteValue();
            SeaTunnelRow row =
                    new SeaTunnelRow(
                            new Object[] {
                                i % 2 == 0 ? (byte) 1 : (byte) 0,
                                new byte[] {byteArr},
                                new byte[] {byteArr, byteArr},
                                new byte[] {byteArr, byteArr, byteArr, byteArr},
                                new byte[] {
                                    byteArr, byteArr, byteArr, byteArr, byteArr, byteArr, byteArr,
                                    byteArr
                                },
                                i % 2 == 0 ? Boolean.TRUE : Boolean.FALSE,
                                i,
                                i,
                                i,
                                i,
                                i,
                                i,
                                i,
                                i,
                                i,
                                Long.parseLong("1"),
                                Long.parseLong("1"),
                                Long.parseLong("1"),
                                BigDecimal.valueOf(i, 0),
                                BigDecimal.valueOf(i, 18),
                                BigDecimal.valueOf(i, 18),
                                Float.parseFloat("1.1"),
                                Float.parseFloat("1.1"),
                                Double.parseDouble("1.1"),
                                Double.parseDouble("1.1"),
                                "f",
                                String.format("f1_%s", i),
                                String.format("f1_%s", i),
                                String.format("f1_%s", i),
                                String.format("f1_%s", i),
                                String.format("{\"aa\":\"bb_%s\"}", i),
                                String.format("f1_%s", i),
                                Date.valueOf(LocalDate.now()),
                                Timestamp.valueOf(LocalDateTime.now()),
                                new Timestamp(System.currentTimeMillis()),
                                "test".getBytes(),
                                "test".getBytes(),
                                "test".getBytes(),
                                "test".getBytes(),
                                "test".getBytes(),
                                "f".getBytes(),
                                bigintValue.add(BigDecimal.valueOf(i)),
                                decimalValue.add(BigDecimal.valueOf(i)),
                                decimalValue.add(BigDecimal.valueOf(i)),
                            });
            rows.add(row);
        }

        return Pair.of(fieldNames, rows);
    }

    @Override
    GenericContainer<?> initContainer() {
        GenericContainer<?> container =
                new GenericContainer<>(OCEANBASE_IMAGE)
                        .withNetwork(NETWORK)
                        .withNetworkAliases(OCEANBASE_CONTAINER_HOST)
                        .withLogConsumer(
                                new Slf4jLogConsumer(
                                        DockerLoggerFactory.getLogger(OCEANBASE_IMAGE)));

        container.setPortBindings(Lists.newArrayList(String.format("%s:%s", PORT, PORT)));

        return container;
    }

    @Override
    protected void createSchemaIfNeeded() {
        String sql = "CREATE DATABASE IF NOT EXISTS " + DATABASE;
        try {
            connection.prepareStatement(sql).executeUpdate();
        } catch (Exception e) {
            throw new SeaTunnelRuntimeException(
                    JdbcITErrorCode.CREATE_TABLE_FAILED, "Fail to execute sql " + sql, e);
        }
    }
}
