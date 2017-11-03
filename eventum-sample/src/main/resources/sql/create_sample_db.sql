CREATE DATABASE eventum_sample;

USE eventum_sample;

CREATE TABLE sample_orders(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    orderId VARCHAR(64) NOT NULL COMMENT 'order id',
    customerId VARCHAR(64) NOT NULL COMMENT 'the customer id',
    status VARCHAR(16) NOT NULL COMMENT 'order status',
    created_time TIMESTAMP NOT NULL COMMENT 'the order created time',
    modified_time TIMESTAMP NOT NULL COMMENT 'the order modified time'
)ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'sample order table';

CREATE UNIQUE INDEX uni_idx_orderId ON  sample_orders (orderId);

CREATE TABLE eventum_events(
    id VARCHAR(64) NOT NULL PRIMARY KEY COMMENT 'eventum id',
    type VARCHAR(64) NOT NULL COMMENT 'the event type',
    data VARCHAR(2048)  COMMENT 'the event data',
    metadata VARCHAR(256) COMMENT 'the metadata',
    entityId VARCHAR(64) NOT NULL COMMENT 'to identify an entity',
    status VARCHAR(32) NOT NULL COMMENT 'the event status',
    retries INT NOT NULL COMMENT 'the retry count',
    next_retry_time DATETIME NOT NULL COMMENT 'the event next retry time',
    created_time TIMESTAMP NOT NULL COMMENT 'the event created time',
    modified_time TIMESTAMP NOT NULL COMMENT 'the event modified time'
)ENGINE=INNODB DEFAULT CHARSET=utf8 COMMENT 'eventum event table';

