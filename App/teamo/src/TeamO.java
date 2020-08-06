import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.query.*;

public class TeamO implements ActionListener {
    private static Model model;
    
    public class FancyQuery {
        public final String name;
        public final String info;
        public final String[] dataLabels;
        public final String[] dataValues;
        public final String queryWithPlaceholders;
        // placeholder positions denoted with §0, §1, etc.
        
        public FancyQuery(String name,
                          String info,
                          String[] dataLabels,
                          String[] dataValues,
                          String queryWithPlaceholders) {
            this.name = name;
            this.info = info;
            this.dataLabels = dataLabels;
            this.dataValues = dataValues;
            this.queryWithPlaceholders = queryWithPlaceholders;
        }
        
        public String constructQuery(String[] placeholderValues) {
            String query = queryWithPlaceholders;
            for (int i = 0; i < placeholderValues.length; i++) {
                query = query.replaceAll("§" + i, placeholderValues[i]);
            }
            return query;
        }
    }
    private final FancyQuery[] fancyQueries = new FancyQuery[] {
        new FancyQuery("Q1. Number of Homeless in County with Highest Housing Prices in Year",
                       "What was the number of homeless people in the county with the highest housing prices in the given year?",
                       new String[] {"Year"},
                       new String[] {"2014"},
                       "PREFIX db: <http://dbpedia.org/ontology/> \n" +
                       "PREFIX ont: <http://webprotege.stanford.edu/projectOntology#> \n" +
                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
                       "SELECT ?num ?county ?price WHERE { \n" +
                       "    ?z ont:CountyName ?county . \n" +
                       "    ?z ont:NumOfHomeless ?num . \n" +
                       "    ?z db:Year '§0' . \n" +
                       "    ?x ont:MeanPrice ?price . \n" +
                       "    ?x ont:CountyName ?county . \n" +
                           
                       "    {SELECT ?price WHERE { \n" +
                       "        ?x db:Year '§0' . \n" +
                       "        ?x ont:MeanPrice ?price . \n" +
                       "        ?x ont:CountyName ?county . \n" +
                       "        ?x ont:HousingPricesType 'Execution' . \n" +
                       "    }ORDER BY DESC(xsd:float(?price)) LIMIT 1 \n" +
                       "}}"),
        new FancyQuery("Q2. Rainfall in year with highest Thefts in County",
                       "What was the average precipitation of the given County in the year with the highest number of thefts?",
                       new String[] {"County"},
                       new String[] {"Dublin"},
                       "PREFIX db: <http://dbpedia.org/ontology/> \n" +
                       "PREFIX ont: <http://webprotege.stanford.edu/projectOntology#> \n" +
                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
                       "SELECT DISTINCT ?rain ?year WHERE { \n" +
                       "    ?x db:Year ?year . \n" +
                       "    ?z db:Year ?year . \n" +
                       "    ?z ont:Rainfall ?rain . \n" +
                       "    ?z ont:CountyName '§0' . \n" +
                       "    { \n" +
                       "        SELECT ?num ?year WHERE { \n" +
                       "            ?x ont:NumOfCrime ?num . \n" +
                       "            ?x ont:CrimeName 'Theft and related offences' . \n" +
                       "            ?x db:Year ?year . \n" +
                       "            ?x ont:CountyName '§0' . \n" +
                       "        } ORDER BY DESC(xsd:float(?num)) LIMIT 1 \n" +
                       "    }}"),
        new FancyQuery("Q3. Housing Prices when Salaries at highest",
                       "What was the average cost of housing when salaries were at their highest?",
                       new String[] {"County"},
                       new String[] {"Wicklow"},
                       "PREFIX db: <http://dbpedia.org/ontology/>  \n" +
                       "PREFIX ont: <http://webprotege.stanford.edu/projectOntology#>  \n" +
                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>  \n" +
                       "SELECT ?price ?year WHERE {  \n" +
                       "    ?z db:Year ?year .  \n" +
                       "    ?z ont:MeanPrice ?price .  \n" +
                       "    ?z ont:CountyName '§0' .  \n" +
                       "    ?z ont:HousingPricesType 'Execution' .  \n" +
                       "    {  \n" +
                       "        SELECT ?salary ?year WHERE {  \n" +
                       "            ?x ont:SalaryValue ?salary .  \n" +
                       "            ?x db:Year ?year .  \n" +
                       "            ?x ont:CountyName '§0' .  \n" +
                       "        } ORDER BY DESC(xsd:float(?salary)) LIMIT 1  \n" +
                       "    }}"),
        new FancyQuery("Q4. Least Homeless in Counties with limited Rainfall in Year",
                       "What counties have gotten below the given rainfall in the given year?\n" +
                       "And from these counties which one had the lowest rate of homelessness?",
                       new String[] {"Year", "Rainfall"},
                       new String[] {"2016", "89.58"},
                       "PREFIX db: <http://dbpedia.org/ontology/> \n" +
                       "PREFIX ont: <http://webprotege.stanford.edu/projectOntology#> \n" +
                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
                       "SELECT DISTINCT ?homeless ?county WHERE { \n" +
                       "    ?z ont:CountyName ?county . \n" +
                       "    ?x ont:CountyName ?county . \n" +
                       "    ?z ont:NumOfHomeless ?homeless . \n" +
                       "    ?z db:Year '§0' . \n" +
                       "    { \n" +
                       "        SELECT ?county WHERE { \n" +
                       "            ?x ont:Rainfall ?avgRain . \n" +
                       "            ?x ont:CountyName ?county . \n" +
                       "            ?x db:Year '§0' . \n" +
                       "            FILTER (xsd:float(?avgRain) <§1) \n" +
                       "        }}} ORDER BY ASC(xsd:int(?homeless)) LIMIT 1"),
        new FancyQuery("Q5. What were the average salary and housing prices in Dublin in 2010 vs 2016? ",
                       "What were the average salary and housing prices in county in 2010 vs 2016? ",
                       new String[] {"County"},
                       new String[] {"Dublin"},
                       "PREFIX db: <http://dbpedia.org/ontology/>\n" +
                       "PREFIX ont: <http://webprotege.stanford.edu/projectOntology#>\n" +
                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
                       
                       " SELECT ?price_dub_2010 ?price_dub_2016 ?sal_dub_2010 ?sal_dub_2016\n" +
                       "WHERE{\n" +
                       "    ?x ont:MeanPrice ?price_dub_2010 .\n" +
                       "    ?x db:Year '2010' .\n" +
                       "    ?z ont:MeanPrice ?price_dub_2016 .\n" +
                       "    ?z db:Year '2016' .\n" +
                       "    ?x ont:HousingPricesType 'Execution'.\n" +
                       "    ?x ont:CountyName  '§0' .\n" +
                       "    ?z ont:CountyName  '§0' .\n" +
                       "    ?z ont:HousingPricesType 'Execution'.\n" +
                       "    ?a ont:SalaryValue ?sal_dub_2010 .\n" +
                       "    ?b ont:SalaryValue ?sal_dub_2016 .\n" +
                       "{ SELECT ?sal_dub_2010 ?sal_dub_2016\n" +
                       "WHERE{\n" +
                       "    ?a ont:SalaryValue ?sal_dub_2010 .\n" +
                       "    ?a db:Year '2010' . \n" +
                       "    ?b ont:SalaryValue ?sal_dub_2016 .\n" +
                       "    ?b db:Year '2016' . \n" +
                       "    ?a ont:CountyName  '§0' .\n" +
                       "    ?b ont:CountyName  '§0' .\n" +
                       "}}.\n" +
                       "}"),
        new FancyQuery("Q6. Does the county with the highest salary have more kidnappings than county lowest salary?",
                       "In [insert year] did the county with the highest salary have more kidnappings than the county with the lowest salary?",
                       new String[] {"Year"},
                       new String[] {"2015"},
                       "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                       "PREFIX  ont:  <http://webprotege.stanford.edu/projectOntology#>\n" +
                       "PREFIX  db:   <http://dbpedia.org/ontology/>\n" +
                       "SELECT ?county ?salary ?kidnappings ?county1 ?salary1 ?kidnappings1 WHERE {\n" +
                       "    ?x ont:CountyName ?county .\n" +
                       "    ?x db:Year '§0' .\n" +
                       "    ?x ont:CrimeName 'Kidnapping and related offences' .\n" +
                       "    ?x ont:NumOfCrime ?kidnappings .\n" +
                       "    ?z ont:CountyName ?county1 .\n" +
                       "    ?z db:Year '§0' .\n" +
                       "    ?z ont:CrimeName 'Kidnapping and related offences' .\n" +
                       "    ?z ont:NumOfCrime ?kidnappings1 .\n" +
                       "    {  \n" +
                       "        SELECT ?salary ?county WHERE {\n" +
                       "            ?x ont:CountyName ?county .\n" +
                       "            ?x ont:SalaryValue ?salary .\n" +
                       "            ?x db:Year '§0' .\n" +
                       "        } ORDER BY DESC(xsd:float(?salary)) LIMIT 1 \n" +
                       "    }\n" +
                       "    {  \n" +
                       "        SELECT ?salary1 ?county1 WHERE {\n" +
                       "            ?x ont:CountyName ?county1 .\n" +
                       "            ?x ont:SalaryValue ?salary1 .\n" +
                       "            ?x db:Year '§0' .\n" +
                       "        } ORDER BY ASC(xsd:float(?salary1)) LIMIT 1\n" +
                       "    }}"),
        new FancyQuery("Q7. Number of Homeless in year with the highest Salary",
                       "In the year with the highest average salary how many homeless people were there in the given county?",
                       new String[] {"County"},
                       new String[] {"Cork"},
                       "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>  \n" +
                       "PREFIX  ont:  <http://webprotege.stanford.edu/projectOntology#>  \n" +
                       "PREFIX  db:   <http://dbpedia.org/ontology/>  \n" +
                       "SELECT ?homeless ?year WHERE {  \n" +
                       "    ?z db:Year ?year .  \n" +
                       "    ?z ont:NumOfHomeless ?homeless .  \n" +
                       "    ?z ont:CountyName '§0' .  \n" +
                       "    {  \n" +
                       "        SELECT ?salary ?year WHERE {  \n" +
                       "            ?x ont:SalaryValue ?salary .  \n" +
                       "            ?x db:Year ?year .  \n" +
                       "            ?x ont:CountyName '§0' .  \n" +
                       "        } ORDER BY DESC(xsd:float(?salary)) LIMIT 1  \n" +
                       "    }}"),
        new FancyQuery("Q8. What is the homelessness in Dublin and Crime rate of theft in 2014 vs 2019?",
                       "What is the homelessness in Dublin and Crime rate of theft in 2014 vs 2019",
                       new String[] {"County", "1 Year", "2 Year"},
                       new String[] {"Dublin", "2014", "2019"},
                       "PREFIX  xsd:  <http://www.w3.org/2001/XMLSchema#>\n" +
                       "PREFIX  ont:  <http://webprotege.stanford.edu/projectOntology#>\n" +
                       "PREFIX  db:   <http://dbpedia.org/ontology/>\n" +

                        "SELECT ?homeless_2014 ?homeless_2019 ?crime_2014 ?crime_2019\n" +
                        "WHERE{\n" +
                        "    ?x ont:NumOfHomeless ?homeless_2014 .\n" +
                        "    ?x db:Year '§1' .\n" +
                        "    ?z ont:NumOfHomeless ?homeless_2019 .\n" +
                        "    ?z db:Year '§2' .\n" +
                        "    ?x ont:CountyName '§0' .\n" +
                        "    ?z ont:CountyName '§0' .\n" +
                        "{ SELECT ?crime_2014 ?crime_2019\n" +
                        "WHERE{\n" +
                        "    ?a ont:NumOfCrime ?crime_2014 .\n" +
                        "    ?a db:Year '§1' .\n" +
                        "    ?b ont:NumOfCrime ?crime_2019 .\n" +
                        "    ?b db:Year '§2' .\n" +
                        "    ?a ont:CountyName '§0' .\n" +
                        "    ?b ont:CountyName  '§0' .\n" +
                        "    ?a ont:CrimeName 'Burglary and related offences' .\n" +
                        "    ?b ont:CrimeName 'Burglary and related offences' .\n" +
                        "}}\n" +
                        "}"
                    ),
        new FancyQuery("Q9. What is the housing prices vs crime over years and counties?",
                       "Show housing prices vs crime over years and counties?",
                       new String[] {"Year", "CountyName", "Year"},
                       new String[] {"2010", "Dublin", "2014"},
                       "PREFIX db: <http://dbpedia.org/ontology/> \n" +
                       "PREFIX ont: <http://webprotege.stanford.edu/projectOntology#> \n" +
                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> \n" +
                       "SELECT DISTINCT ?priceYear1 ?crimeYear1 ?priceYear2 ?crimeYear2 WHERE { \n" +
                       "    ?z ont:MeanPrice ?priceYear1 . \n" +
                       "    ?z db:Year '§0' . \n" +
                       "    ?z ont:CountyName '§1' . \n" +
                       "    ?z ont:HousingPricesType 'Execution' . \n" +
                       "    ?w ont:MeanPrice ?priceYear2 . \n" +
                       "    ?w db:Year '§2' . \n" +
                       "    ?w ont:CountyName '§1' . \n" +
                       "    ?w ont:HousingPricesType 'Execution' . \n" +
                       "    { \n" +
                       "        SELECT DISTINCT (SUM(xsd:float(?numYear1)) AS ?crimeYear1) (SUM(xsd:float(?numYear2)) AS ?crimeYear2) WHERE { \n" +
                       "            ?x ont:CrimeName 'Burglary and related offences' . \n" +
                       "            ?x ont:NumOfCrime ?numYear1 . \n" +
                       "            ?x db:Year '§0' . \n" +
                       "            ?x ont:CountyName '§1' . \n" +
                       "            ?y ont:CrimeName 'Burglary and related offences' . \n" +
                       "            ?y ont:NumOfCrime ?numYear2 . \n" +
                       "            ?y db:Year '§2' . \n" +
                       "            ?y ont:CountyName '§1' . \n" +
                       "        }}}"),
        new FancyQuery("Q10. Counties with Year over Year Housing Price increase without Homelessnes increase",
                       "Which Counties saw a year over year increase in housing prices while not seeing an increase in homelessnes?",
                       new String[] {"Housing Price Type"},
                       new String[] {"Execution"},
                       "PREFIX db: <http://dbpedia.org/ontology/>\n" +
                       "PREFIX ont: <http://webprotege.stanford.edu/projectOntology#>\n" +
                       "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                       "SELECT ?old_year ?county ?old_price ?new_price ?old_homeless ?new_homeless WHERE {\n" +
                       "    ?x ont:CountyName ?county .\n" +
                       "    ?x db:Year ?old_year .\n" +
                       "    ?x ont:HousingPricesType '§0' .\n" +
                       "    ?x ont:MeanPrice ?old_price .\n" +
                       "\n" +
                       "    ?y ont:CountyName ?county .\n" +
                       "    ?y db:Year ?b .\n" +
                       "    ?y ont:HousingPricesType '§0' .\n" +
                       "    ?y ont:MeanPrice ?new_price .\n" +
                       "\n" +
                       "    ?s ont:CountyName ?county .\n" +
                       "    ?s db:Year ?old_year .\n" +
                       "    ?s ont:NumOfHomeless ?old_homeless .\n" +
                       "\n" +
                       "    ?t ont:CountyName ?county .\n" +
                       "    ?t db:Year ?b .\n" +
                       "    ?t ont:NumOfHomeless ?new_homeless .\n" +
                       "\n" +
                       "    FILTER (xsd:int(?old_year) + 1 = xsd:int(?b))\n" +
                       "    FILTER (xsd:float(?old_price) < xsd:float(?new_price))\n" +
                       "    FILTER (xsd:float(?old_homeless) >= xsd:float(?new_homeless))\n" +
                       "}")
    };
    
    private JFrame mainWindow;
    
    private JComboBox fancyQueryComboBox;
    private JLabel fancyQueryInfoLabel;
    private JPanel fancyQueryDataPanel;
    private JLabel[] fancyQueryDataLabels = new JLabel[0];
    private JTextField[] fancyQueryDataFields = new JTextField[0];
    private JButton fancyQueryButton;
    
    private JScrollPane basicQueryScrollPane;
    private JTextArea basicQueryField;
    private JButton basicQueryButton;
    
    private JScrollPane resultsScrollPane;
    private JTextArea resultsField;
    
    public static void main(String[] args) {
        model = ModelFactory.createDefaultModel();
        model.read("all.ttl");
        
        new TeamO();
    }
    
    private TeamO() {
        try {
            UIManager.setLookAndFeel(
                UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {}
        
        mainWindow = new JFrame();
        mainWindow.setTitle("Team O");
        mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainWindow.setLocationRelativeTo(null);
        mainWindow.setMinimumSize(new Dimension(300, 500));
        mainWindow.getContentPane()
                  .setLayout(new GridBagLayout());

        String[] fancyQueryNames = new String[fancyQueries.length];
        for (int i = 0; i < fancyQueries.length; i++)
            fancyQueryNames[i] = fancyQueries[i].name;
        
        fancyQueryComboBox = new JComboBox(fancyQueryNames);
        fancyQueryComboBox.setSelectedIndex(0);
        fancyQueryComboBox.addActionListener(this);
        fancyQueryInfoLabel = new JLabel("", SwingConstants.LEFT);
        fancyQueryInfoLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        fancyQueryDataPanel = new JPanel();
        fancyQueryDataPanel.setLayout(new GridBagLayout());
        fancyQueryButton = new JButton("Generate and Run Query");
        fancyQueryButton.addActionListener(this);
        
        GridBagConstraints fancyQueryComboBoxConstraints = new GridBagConstraints();
        fancyQueryComboBoxConstraints.gridx = 0;
        fancyQueryComboBoxConstraints.gridy = 0;
        fancyQueryComboBoxConstraints.gridwidth = 2;
        fancyQueryComboBoxConstraints.weightx = 1.0;
        fancyQueryComboBoxConstraints.insets = new Insets(10, 10, 5, 10);
        fancyQueryComboBoxConstraints.fill = GridBagConstraints.HORIZONTAL;
        
        GridBagConstraints fancyQueryInfoLabelConstraints = new GridBagConstraints();
        fancyQueryInfoLabelConstraints.gridx = 0;
        fancyQueryInfoLabelConstraints.gridy = 1;
        fancyQueryInfoLabelConstraints.gridwidth = 2;
        fancyQueryInfoLabelConstraints.weightx = 1.0;
        fancyQueryInfoLabelConstraints.insets = new Insets(0, 10, 5, 10);
        fancyQueryInfoLabelConstraints.fill = GridBagConstraints.HORIZONTAL;
        
        GridBagConstraints fancyQueryDataPanelConstraints = new GridBagConstraints();
        fancyQueryDataPanelConstraints.gridx = 0;
        fancyQueryDataPanelConstraints.gridy = 2;
        fancyQueryDataPanelConstraints.gridwidth = 2;
        fancyQueryDataPanelConstraints.weightx = 1.0;
        fancyQueryDataPanelConstraints.insets = new Insets(0, 10, 0, 10);
        fancyQueryDataPanelConstraints.fill = GridBagConstraints.HORIZONTAL;
        
        GridBagConstraints fancyQueryButtonConstraints = new GridBagConstraints();
        fancyQueryButtonConstraints.gridx = 1;
        fancyQueryButtonConstraints.gridy = 3;
        fancyQueryButtonConstraints.insets = new Insets(0, 10, 10, 10);
        fancyQueryButtonConstraints.anchor = GridBagConstraints.LINE_END;
        
        mainWindow.add(fancyQueryComboBox, fancyQueryComboBoxConstraints);
        mainWindow.add(fancyQueryInfoLabel, fancyQueryInfoLabelConstraints);
        mainWindow.add(fancyQueryDataPanel, fancyQueryDataPanelConstraints);
        mainWindow.add(fancyQueryButton, fancyQueryButtonConstraints);
        
        basicQueryField = new JTextArea();
        basicQueryScrollPane = new JScrollPane(basicQueryField);
        basicQueryScrollPane.setPreferredSize(new Dimension(200, 50));
        basicQueryButton = new JButton("Run Query");
        basicQueryButton.addActionListener(this);

        GridBagConstraints basicQueryScrollPaneConstraints = new GridBagConstraints();
        basicQueryScrollPaneConstraints.gridx = 0;
        basicQueryScrollPaneConstraints.gridy = 4;
        basicQueryScrollPaneConstraints.gridwidth = 2;
        basicQueryScrollPaneConstraints.weightx = 1.0;
        basicQueryScrollPaneConstraints.weighty = 1.0;
        basicQueryScrollPaneConstraints.insets = new Insets(10, 10, 5, 10);
        basicQueryScrollPaneConstraints.fill = GridBagConstraints.BOTH;
        
        GridBagConstraints basicQueryButtonConstraints = new GridBagConstraints();
        basicQueryButtonConstraints.gridx = 1;
        basicQueryButtonConstraints.gridy = 5;
        basicQueryButtonConstraints.insets = new Insets(0, 10, 10, 10);
        basicQueryButtonConstraints.anchor = GridBagConstraints.LINE_END;
        
        mainWindow.add(basicQueryScrollPane, basicQueryScrollPaneConstraints);
        mainWindow.add(basicQueryButton, basicQueryButtonConstraints);
        
        resultsField = new JTextArea();
        resultsField.setEditable(false);
        resultsScrollPane = new JScrollPane(resultsField);
        resultsScrollPane.setPreferredSize(new Dimension(200, 50));
        
        GridBagConstraints resultsScrollPaneConstraints = new GridBagConstraints();
        resultsScrollPaneConstraints.gridx = 0;
        resultsScrollPaneConstraints.gridy = 6;
        resultsScrollPaneConstraints.gridwidth = 2;
        resultsScrollPaneConstraints.weightx = 1.0;
        resultsScrollPaneConstraints.weighty = 1.0;
        resultsScrollPaneConstraints.insets = new Insets(10, 10, 10, 10);
        resultsScrollPaneConstraints.fill = GridBagConstraints.BOTH;
        
        mainWindow.add(resultsScrollPane, resultsScrollPaneConstraints);
        
        updateFancyQueryPanel();
        
        mainWindow.pack();
        mainWindow.show();
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == fancyQueryComboBox) {
            updateFancyQueryPanel();
        } else if (e.getSource() == fancyQueryButton) {
            int fancyQueryIndex = fancyQueryComboBox.getSelectedIndex();
            FancyQuery fancyQuery = fancyQueries[fancyQueryIndex];
            
            String[] replacements = new String[fancyQuery.dataLabels.length];
            for (int i = 0; i < replacements.length; i++)
                replacements[i] = fancyQueryDataFields[i].getText();
            
            String query = fancyQuery.constructQuery(replacements);
            basicQueryField.setText(query);
            runQuery(query);
            
        } else if (e.getSource() == basicQueryButton) {
            String query = basicQueryField.getText();
            runQuery(query);
        }
    }
    
    private void updateFancyQueryPanel() {
        int fancyQueryIndex = fancyQueryComboBox.getSelectedIndex();
        FancyQuery fancyQuery = fancyQueries[fancyQueryIndex];
        
        fancyQueryInfoLabel.setText("<html>"+fancyQuery.info+"</html>");
        basicQueryField.setText(fancyQuery.constructQuery(fancyQuery.dataValues));
        
        for (int i = 0; i < fancyQueryDataLabels.length; i++) {
            fancyQueryDataPanel.remove(fancyQueryDataLabels[i]);
        }
        for (int i = 0; i < fancyQueryDataFields.length; i++) {
            fancyQueryDataPanel.remove(fancyQueryDataFields[i]);
        }
        
        int n = fancyQuery.dataLabels.length;
        fancyQueryDataLabels = new JLabel[n];
        fancyQueryDataFields = new JTextField[n];
        for (int i = 0; i < n; i++) {
            JLabel label = new JLabel(fancyQuery.dataLabels[i] + ":", SwingConstants.RIGHT);
            JTextField field = new JTextField(fancyQuery.dataValues[i]);
            
            GridBagConstraints labelConstraints = new GridBagConstraints();
            labelConstraints.gridx = 0;
            labelConstraints.gridy = i;
            labelConstraints.insets = new Insets(0, 0, 5, 0);
            labelConstraints.fill = GridBagConstraints.HORIZONTAL;
            GridBagConstraints fieldConstraints = new GridBagConstraints();
            fieldConstraints.gridx = 1;
            fieldConstraints.gridy = i;
            fieldConstraints.weightx = 1.0;
            fieldConstraints.insets = new Insets(0, 5, 5, 0);
            fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
            
            fancyQueryDataLabels[i] = label;
            fancyQueryDataFields[i] = field;
            
            fancyQueryDataPanel.add(label, labelConstraints);
            fancyQueryDataPanel.add(field, fieldConstraints);
        }
        
        mainWindow.revalidate();
    }
    
    private void runQuery(String queryString) {
        System.out.println("query:\n" + queryString);
        
        String resultString = "";
        try {
            Query query = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.create(query, model);
            ResultSet results = qexec.execSelect();
            int i;
            for (i = 0; i < 1000 && results.hasNext(); i++) {
                QuerySolution soln = results.nextSolution();
                resultString += soln.toString() + "\n";
                RDFNode x = soln.get("varName");       // Get a result variable by name.
                Resource r = soln.getResource("VarR"); // Get a result variable - must be a resource
                Literal l = soln.getLiteral("VarL");   // Get a result variable - must be a literal
            }
            if (results.hasNext()) {
                resultString += "... (over 1000 results)";
            } else {
                resultString += "(" + i + " results)";
            }
        } catch (Exception ex) {
            resultString += ex.toString();
        }
        
        System.out.println("results:\n" + resultString);
        
        resultsField.setText(resultString);
    }
    
}
