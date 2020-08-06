### Knowledge and Data Engineering Ontology

Ontology represents crime, housing prices, average salaries, weather and homelessness for different Counties and years.
Application helps provide information on statistics and make correlations or connections between the different statistics.

# 1. RDF Mappings
https://github.com/alexSilvaa/data_engineering/blob/master/r2rml.pdf

# 2. Ontology in Turtle
https://github.com/alexSilvaa/data_engineering/blob/master/ontology.ttl

# 3. Ontology in Owl
https://github.com/alexSilvaa/data_engineering/blob/master/OwlOntology.owl

# 4. SPARQL Queries
https://github.com/alexSilvaa/data_engineering/tree/master/Queries

# 5. Application for creating new and running existing SPARQL queries on Ontology
https://github.com/alexSilvaa/data_engineering/tree/master/App/teamo


## Application User Interface 
![alt text](https://github.com/alexSilvaa/data_engineering/blob/master/appUI.png)

The combo box at the top has a selection of prepared queries. Directly below is a longer description of the selected query, followed by zero or more text fields to fill specific parts of the query. In the image above, the user can enter a year and rainfall amount (in mm) that will be included in the query. The button “Generate and Run Query” then generates a query with the given values, updates the text area below with the generated query, runs the query, and displays the results.
The Application uses Apache Jena to load our model (ttl file) and run SPARQL queries. The User Interface was built with the Java Swing library.
