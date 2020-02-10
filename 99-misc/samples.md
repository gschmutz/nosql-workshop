# Order Processing Sample Implementation for various Data Stores

## RDBMS

Connect to PostgreSQL

```
docker exec -ti postgresql psql -d sample -U sample
```

Create the tables

```
CREATE TABLE order_t (id INTEGER
					, order_date TIMESTAMP
					, delivery_address_id INTEGER
					, billing_address_id INTEGER);
ALTER TABLE "order_t"
ADD CONSTRAINT "order_pk" PRIMARY KEY ("id");

CREATE TABLE order_line_t (id INTEGER
						, order_id INTEGER
						, product_id INTEGER
						, qty INTEGER
						, price MONEY);

ALTER TABLE "order_line_t"
ADD CONSTRAINT "order_line_pk" PRIMARY KEY ("id");

CREATE TABLE customer_t (id INTEGER
						, first_name VARCHAR(50)
						, last_name VARCHAR(50)
						, gender VARCHAR(10)
						);
ALTER TABLE "customer_t"
ADD CONSTRAINT "customer_pk" PRIMARY KEY ("id");
						
CREATE TABLE product_t (id INTEGER
						, category_id INTEGER
						, name VARCHAR(50)
						, price MONEY
						, description VARCHAR(1000));
						
ALTER TABLE "product_t"
ADD CONSTRAINT "product_pk" PRIMARY KEY ("id");
						
CREATE TABLE category_t (id INTEGER
						, name VARCHAR(20));
						
ALTER TABLE "category_t"
ADD CONSTRAINT "category_pk" PRIMARY KEY ("id");

ALTER TABLE "order_line_t"
ADD FOREIGN KEY ("order_id") REFERENCES "order_t" ("id") ON DELETE CASCADE;
```
	
Adding the data	
	
```
INSERT INTO category_t (id, name) VALUES (1, 'Electronics' );
INSERT INTO category_t (id, name) VALUES (2, 'Sport' );
INSERT INTO category_t (id, name) VALUES (3, 'Food' );

INSERT INTO product_t (id, category_id, name, price, description) VALUES (101, 1, 'APPLE IPad 32GB', 483.65, 'APPLE IPad 32GB ...');
INSERT INTO product_t (id, category_id, name, price, description) VALUES (102, 2, 'AQUAMARINA SUP', 299.00, 'AQUAMARINA SUP ...');
INSERT INTO product_t (id, category_id, name, price, description) VALUES (103, 3, 'Ricola Lakrize', 2.90, 'Ricola Lakrize ...');	
						
INSERT INTO order_t (id, order_date) VALUES (1, CURRENT_DATE);

INSERT INTO order_line_t (id, order_id, product_id, qty, price) VALUES (1, 1, 101, 1, 483.65);
INSERT INTO order_line_t (id, order_id, product_id, qty, price) VALUES (2, 1, 103, 2, 2.90);
```

### Queries

Get all Products

```
SELECT * 
FROM product_t;
```

Get Products by Category

```
SELECT prod.* 
FROM product_t	prod
LEFT JOIN category_t	cat
ON (prod.category_id = cat.id)
WHERE cat.name = 'Electronics';
```

Get Product by Id

```
SELECT prod.*, cat.name
FROM product_t	prod
LEFT JOIN category_t	cat
ON (prod.category_id = cat.id)
WHERE prod.id = 102;
```

Get Customer by Id

```
SELECT *
FROM customer_t	cust
WHERE cust.id = 20003;
```


## Redis

```
docker exec -ti redis redis-cli
```

Adding Products

```
SET prod:1001 '{ "productId": "101", "name": "APPLE IPad 32GB", "description": "APPLE IPad 32GB ...", "price": 483.65, "category": "Electronics"}'

SET prod:1002 '{ "productId": "102", "name": "AQUAMARINA SUP", "description": "AQUAMARINA SUP ...", "price": 299.00, "category": "Sport"}'

SET prod:1003 '{ "productId": "103", "name": "Ricola Lakrize", "description": "Ricola Lakrize ...", "price": 2.90, "category": "Food"}'
```

Adding Customers

```
SET cust:20001 '{ "customerId": "20001", "firstName": "Peter", "lastName": "Muster", "gender" : "male", "addresses": [10, 21]}'

SET cust:20002 '{ "customerId": "20002", "firstName": "Barbara", "lastName": "Muster", "gender" : "female", "addresses": [10, 21]}'

SET cust:20003 '{ "customerId": "20003", "firstName": "Paul", "lastName": "Scott", "gender" : "male", "addresses": [30]}'
```

Adding Addresses

```
SET addr:10 '{ "addressId": "10", "street": "Musterstrasse", "nr": "5", "zipCode" : "3001", "city": "Somewhere", "country": "CH"}'

SET addr:11 '{ "addressId": "11", "street": "Mustergasse", "nr": "1", "zipCode" : "9092", "city": "Otherwhere", "country": "CH"}'
```

Adding Orders

```
SET ord:13232 '{"orderId": "13232", "orderDate": "10.1.2018", "customerId": 101, "deliveryAddressId": 10, "billingAddressId": 10, "orderDetails": [ { "productid": 101, "quantity": 1, "price": 483.65 }, { "productid": 103, "quantity": 1, "price": 2.90 } ]}'

SET ord:13233 '{"orderId": "13233", "orderDate": "10.2.2018", "customerId": 102, "deliveryAddressId": 11, "billingAddressId": 10, "orderDetails": [ { "productid": 103, "quantity": 3, "price": 2.90 } ]}'

```

Adding Categories

```
SET cat:electronics '{ "category": "electronics", "categories: [101, 104] }'
SET cat:sport '{ "category": "sport", categories: [102, 105, 106] }'
SET cat:food '{ "category": "food", categories: [103, 108] }'
```


## Cassandra

Create Keyspace

```
CREATE KEYSPACE orderprocessing WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'} AND durable_writes = true;
```

Create Tables

```
USE orderprocessing;

CREATE TABLE product (product_id int
						, name text
						, description text
						, price decimal
						, category text
						, PRIMARY KEY (product_id));
						
CREATE TABLE customer (customer_id int
						, first_name text static
						, last_name text static
						, gender text static
						, address_id int
						, street text
						, nr text
						, zip_code text
						, city text
						, country text
						, PRIMARY KEY (customer_id, address_id));
						
CREATE TABLE orders (order_id int
						, order_date timestamp static
						, cust_id int static
						, deliv_id int static
						, deliv_street text static
						, deliv_zipcode text static
						, deliv_city text static
						, deliv_country text static
						, bill_id int static
						, bill_street text static
						, bill_zipcode text static
						, bill_city text static
						, bill_country text static
						, item_nr int
						, prod_id int
						, qty int
						, price decimal
						, PRIMARY KEY (order_id, item_nr));

CREATE TABLE product_by_category (category text
						, product_id int
						, PRIMARY KEY (category, product_id));
```

Adding product

```
INSERT INTO product (product_id, name, description, price, category) VALUES (101, 'APPLE IPad 32GB', 'AQUAMARINA SUP ...', 299.00, 'Electronics');

INSERT INTO product (product_id, name, description, price, category) VALUES (102, 'AQUAMARINA SUP', 'APPLE IPad 32GB ...', 483.65, 'Sport');

INSERT INTO product (product_id, name, description, price, category) VALUES (103, 'Ricola Lakrize', 'Ricola Lakrize ...', 2.90, 'Food');
```

Adding customer

```
INSERT INTO customer (customer_id, first_name, last_name, gender, address_id, street, nr, zip_code, city, country) VALUES (20001, 'Peter', 'Muster', 'male', 10, 'Musterstrasse', '5', '3001', 'Somewhere', 'CH');

INSERT INTO customer (customer_id, first_name, last_name, gender, address_id, street, nr, zip_code, city, country) VALUES (20001, 'Peter', 'Muster', 'male', 11, 'Mustergasse', '1', '9092', 'Otherwhere', 'CH');

INSERT INTO customer (customer_id, first_name, last_name, gender, address_id, street, nr, zip_code, city, country) VALUES (20002, 'Barbara', 'Muster', 'female', 10, 'Musterstrasse', '5', '3001', 'Somewhere', 'CH');

INSERT INTO customer (customer_id, first_name, last_name, gender, address_id, street, nr, zip_code, city, country) VALUES (20002, 'Barbara', 'Muster', 'female', 11, 'Mustergasse', '1', '9092', 'Otherwhere', 'CH');

INSERT INTO customer (customer_id, first_name, last_name, gender, address_id, street, nr, zip_code, city, country) VALUES (20003, 'Paul', 'Scott', 'male', 30, 'Dorfstrasse', '33', '4044', 'AtTheLake', 'CH');
```

Adding orders

```
INSERT INTO orders (order_id, order_date, cust_id , deliv_id, deliv_street, deliv_zipcode, deliv_city, deliv_country, bill_id, bill_street, bill_zipcode, bill_city, bill_country, item_nr, prod_id, qty, price) VALUES (13232, '2018-01-10', 101, 10, 'Musterstrasse 5', '3001', 'Somewhere', 'CH', 10, 'Musterstrasse 5', '3001', 'Somewhere', 'CH', 1, 101, 1, 483.65); 

INSERT INTO orders (order_id, order_date, cust_id , deliv_id, deliv_street, deliv_zipcode, deliv_city, deliv_country, bill_id, bill_street, bill_zipcode, bill_city, bill_country, item_nr, prod_id, qty, price) VALUES (13232, '2018-01-10', 101, 10, 'Musterstrasse 5', '3001', 'Somewhere', 'CH', 10, 'Musterstrasse 5', '3001', 'Somewhere', 'CH', 2, 103, 1, 2.90); 
```

Adding product_by_category

```
INSERT INTO product_by_category (category, product_id) VALUES ('Electronics', 101);

INSERT INTO product_by_category (category, product_id) VALUES ('Electronics', 104);

INSERT INTO product_by_category (category, product_id) VALUES ('Sport', 102);

INSERT INTO product_by_category (category, product_id) VALUES ('Sport', 105);

INSERT INTO product_by_category (category, product_id) VALUES ('Sport', 106);

INSERT INTO product_by_category (category, product_id) VALUES ('Food', 103);

INSERT INTO product_by_category (category, product_id) VALUES ('Food', 108);
```


## MongoDB

```
docker exec -ti mongo mongo
```

```
use orderdb
```

insert product documents

```
db.products.insert (
{ 
"_id": 101
, "name": "APPLE IPad 32GB"
, "description": "APPLE IPad 32GB ..."
, "price": 483.65
, "category": "Electronics"
})

db.products.insert (
{ 
"_id": 102
, "name": "AQUAMARINA SUP"
, "description": "AQUAMARINA SUP ..."
, "price": 299.00
, "category": "Sport"
})

db.products.insert (
{ 
"_id": 103
, "name": "Ricola Lakrize"
, "description": "Ricola Lakrize ..."
, "price": 2.90
, "category": "Food"
})
```

insert customer documents

```
db.customers.insert (
{ 
"_id": 20001
, "firstName": "Peter"
, "lastName": "Muster"
, "gender": "male"
, "addresses": [ { "id": 10
					, "street": "Musterstrasse"
					, "nr": "5"
					, "zipCode": "3001"
					, "city": "Somewhere"
					, "country": "CH" }
				, { "id": 21
					, "street": "Mustergasse"
					, "nr": "1"
					, "zipCode": "9092"
					, "city": "Otherwhere"
					, "country": "CH" 
					} 
 ]
})

db.customers.insert (
{ 
"_id": 20002
, "firstName": "Barbara"
, "lastName": "Muster"
, "gender": "female"
, "addresses": [ { "id": 10
					, "street": "Musterstrasse"
					, "nr": "5"
					, "zipCode": "3001"
					, "city": "Somewhere"
					, "country": "CH" }
				, { "id": 21
					, "street": "Mustergasse"
					, "nr": "1"
					, "zipCode": "9092"
					, "city": "Otherwhere"
					, "country": "CH" 
					} 
 ]
})

db.customers.insert (
{ 
"_id": 20003
, "firstName": "Paul"
, "lastName": "Scott"
, "gender": "male"
, "addresses": [ { "id": 30
					, "street": "Dorfstrasse"
					, "nr": "33"
					, "zipCode": "4044"
					, "city": "AtTheLake"
					, "country": "CH" }
 ]
})
```

insert order documents

```
db.orders.insert (
{ 
"_id": 13232
, "orderDate": "2018-01-10"
, "customerId": 101
, "deliveryAddress": { "id": 10
					, "street": "Musterstrasse"
					, "nr": "5"
					, "zipCode": "3001"
					, "city": "Somewhere"
					, "country": "CH" }
, "billingAddress": { "id": 10
					, "street": "Musterstrasse"
					, "nr": "5"
					, "zipCode": "3001"
					, "city": "Somewhere"
					, "country": "CH" }
, "orderDetails": [ { "productId": 101
						, "quantity": 1
						, "price": 110.00 }
					, { "productId": 103
						, "quantity": 1
						, "price": 2.90 } 
 ]
})

db.orders.insert (
{ 
"_id": 13233
, "orderDate": "2018-02-10"
, "customerId": 102
, "deliveryAddress": { "id": 11
					, "street": "Mustergasse"
					, "nr": "1"
					, "zipCode": "4042"
					, "city": "Otherwhere"
					, "country": "CH" }
, "billingAddress": { "id": 10
					, "street": "Musterstrasse"
					, "nr": "5"
					, "zipCode": "3001"
					, "city": "Somewhere"
					, "country": "CH" }
, "orderDetails": [ { "productId": 103
						, "quantity": 3
						, "price": 2.90 } 
 ]
})
```


create index on products category

```
db.products.createIndex( {category: 1} );
```

### Queries

Get all Products

```
db.products.find()
```

Get Products by Category

```
db.products.find( {"category": "Electronics"} )
```

Get Product by Id

```
db.products.find( {"_id": 102} )
```

Get Customer by Id

```
db.customers.find( {"_id": 20002} )
```


## Neo4J

In a browser window, navigate to <http://dataplatform:7474> and you should directly land on the Neo4j Browser login screen. 

Enter `bolt://dataplatform:7687` into the **Connect URL**, `neo4j` into the **Username** and `abc123!` into the **Password** field and click **Connect**. 

Create Nodes and Relationships

```
CREATE (Electronics:Category {name: 'Electronics'})
CREATE (Sport:Category {name: 'Sport'})
CREATE (Food:Category {name: 'Food'})

CREATE (Male:Gender {name: 'male'})
CREATE (Female:Gender {name: 'female'})

CREATE (CH:Country {name: 'Switzerland', code: 'CH'})
CREATE (GE:Country {name: 'Germany', code: 'GE'})
CREATE (IT:Country {name: 'Italy', code: 'IT'})
CREATE (AU:Country {name: 'Austria', code: 'AU'})

CREATE (IPad:Product {id: 101, name: 'APPLE IPad 32GB', description: 'APPLE IPad 32GB ...', price: 483.65})
CREATE (Sup:Product {id: 102, name: 'AQUAMARINA SUP', description: 'AQUAMARINA SUP ...', price: 299.00})
CREATE (Ricola:Product {id: 103, name: 'Ricola Lakrize', description: 'Ricola Lakrize ...', price: 2.90})

CREATE (Peter:Customer {id: 20001, firstName: 'Peter', lastName: 'Muster'})
CREATE (Barbara:Customer {id: 20002, firstName: 'Barbara', lastName: 'Muster'})
CREATE (Paul:Customer {id: 20003, firstName: 'Paul', lastName: 'Scott'})

CREATE (Adr10:Address {id: 10, street: 'Musterstrasse', nr: '5', zipCode: '3001', city: 'Somewhere' })
CREATE (Adr11:Address {id: 11, street: 'Mustergasse', nr: '1', zipCode: '9092', city: 'Otherwhere' })
CREATE (Adr30:Address {id: 30, street: 'Dorfstrasse', nr: '33', zipCode: '4042', city: 'AtTheLake' })

CREATE (Ord1:Order {id: 13232 })
CREATE (Ord2:Order {id: 13233 })

CREATE
(IPad)-[:BELONGS_TO]->(Electronics),
(Sup)-[:BELONGS_TO]->(Sport),
(Ricola)-[:BELONGS_TO]->(Food)

CREATE
(Ord1)-[:ORDER]->(IPad),
(Ord1)-[:ORDER]->(Ricola),
(Ord1)-[:BOUGHT_BY]->(Peter),
(Ord1)-[:USE {type: 'deliver'}]->(Adr10),
(Ord1)-[:USE {type: 'bill'}]->(Adr10)

CREATE
(Peter)-[:HAS]->(Adr10),
(Peter)-[:HAS]->(Adr11),
(Barbara)-[:HAS]->(Adr10),
(Barbara)-[:HAS]->(Adr11),
(Paul)-[:HAS]->(Adr30)

CREATE
(Peter)-[:IS]->(Male),
(Barbara)-[:IS]->(Female),
(Paul)-[:IS]->(Male)

CREATE
(Adr10)-[:BELONGS_TO]->(CH),
(Adr11)-[:BELONGS_TO]->(CH),
(Adr30)-[:BELONGS_TO]->(CH)
```

show schema

```
call db.schema.visualization
```

### Queries

Get all Products

```
MATCH (p:Product) RETURN p;
```

Get Products by Category

```
MATCH (c:Category { name: 'Electronics'})<-[:BELONGS_TO]-(p) 
RETURN p;
```

Get Product by Id

```
MATCH (p:Product { id: 101}) 
RETURN p;
```

Get Customer by Id

```
MATCH (c:Customer ) 
WHERE c.id = 20003
RETURN c;
```







