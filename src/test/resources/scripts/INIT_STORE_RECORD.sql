INSERT INTO products (name,description,image_url,price) VALUES
	 ('Eco Gadget 690','Description for product_1','product_1.png',464.64),
	 ('Ultra Gadget 123','Description for product_2','product_2.png',191.38),
	 ('Smart Object 598','Description for product_3','product_3.png',357.76),
	 ('Ultra Widget 734','Description for product_4','product_4.png',84.71),
	 ('Super Item 561','Description for product_5','product_5.png',432.91),
	 ('Ultra Item 402','Description for product_6','product_6.png',289.61),
	 ('Ultra Object 450','Description for product_7','product_7.png',31.17),
	 ('Ultra Object 457','Description for product_8','product_8.png',334.53),
	 ('Eco Thing 576','Description for product_9','product_9.png',211.65),
	 ('Eco Item 319','Description for product_10','product_10.png',301.50);


INSERT INTO cart_items(product_id, quantity) VALUES
    (2, 10),
    (4, 20),
    (6, 30),
    (9, 1);


INSERT INTO orders(total_price) VALUES
    (6319,05),
    (677,68),
    (6210,91);

INSERT INTO order_items(product_id, quantity, price, order_id) VALUES
    (1, 10, 464.64, 1),
    (8, 5, 334.53, 1),
    (4, 2, 84.71, 2),
    (7, 3, 31.17, 3),
    (3, 10, 357.76, 3),
    (9, 12, 211.65, 3);