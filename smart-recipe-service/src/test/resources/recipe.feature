Feature: recipe test

  Scenario: check if service is up
    Given client wants to health check service
    When the client calls the end point to check
    Then the client receives response 'Alive'

  Scenario: create simple recipe
    Given client want to create a recipe
        | name | description | comment |
        | Boeuf bourguignon    | Un bon boaeuf carotte qui démonte    | le faire cuire au moins 7h  |
    When client save recipe
    Then the client receives recipes created

  Scenario: load recipe by id
    Given client wants to load recipe by id '1'
    When client load the recipe by id
    Then client receives recipe not null

  Scenario: load recipe by key keyword
    Given client wants to load recipe by name 'Boeuf bourguignon'
    When client load the recipe
    Then client receives recipe with name 'Boeuf bourguignon'

  Scenario: update recipe
    Given client wants to update recipe by name 'Boeuf bourguignon'
    When client load and update the recipe with new name, description and comment 'Boeuf bourguignon modified'
    Then client receives updated recipe with name 'Boeuf bourguignon modified'

  Scenario: load recipe modified before
      Given client wants to load recipe by name 'Boeuf bourguignon modified'
      When client load the recipe
      Then client receives recipe with name 'Boeuf bourguignon modified'

  Scenario: Create recipe with OCR from recipe binary entity
       Given client wants to create a recipe from image and name 'poulet avec carottes'
       When client save recipe with recipe binary entity
       Then the client receives recipes created with autodescription from image text

  Scenario: Create recipe with OCR from byte array
       Given client wants to create a recipe from image and name 'porc au choux rouge'
       When client save recipe with image as bytes array
       Then the client receives recipes created with autodescription from image text

  Scenario: load compressed light recipe by name
     Given client wants to load recipe by name 'poulet avec carottes'
     When client loads the light compressed recipe by keyword
     Then client receives compressed light recipe with name containing 'poulet avec carottes'

 Scenario: delete all for test data reset
     Given client wants to delete all recipes to reset test data
     When client run delete all recipes
     Then client got no recipe with find all

  Scenario: create n recipes to prepare search testing
     Given client want to create a recipe
          | name | description | comment |
          | Boeuf bourguignon    | Un bon boaeuf carotte qui démonte    | le faire cuire au moins 7h  |
          | Xiao Long Bao    | Des dim sum très bon avec du porc, du gingembre et du vin chinois, soja   | faire cuire 6min  |
          | Poulet à la bière    | Un poulet avec de la bière et des carottes   | cuisson lente requise  |
          | Raclette    | Raclette fumé et aux poivres avec pommes de terre    | A deguster par tout temps ! |
          | Riz sauté    | A cuire avec légumes et un oeuf, un peu de sauce soja à la fin. Pas de sel mais du poivre  | pour finir le frigo |
     When client save recipe
     Then the client receives recipes created

  Scenario: load recipe by recipe name
     Given client wants to load recipe by keyword 'Xiao Long'
     When client load the recipe
     Then client receives recipe with name 'Xiao Long Bao'

   Scenario: load recipe by description
     Given client wants to load recipe by keyword 'pommes de terre'
     When client load the recipe
     Then client receives recipe with name 'Raclette'

  Scenario: load recipe with approximate wrong letter
       Given client wants to load recipe by keyword 'gingambre'
       When client load the recipe
       Then client receives recipe with name 'Xiao Long Bao'

  Scenario: load recipe with approximate accent
       Given client wants to load recipe by keyword 'legumes'
       When client load the recipe
       Then client receives recipe with name 'Riz sauté'

   Scenario: load recipe with approximate missing letter
       Given client wants to load recipe by keyword 'légume'
       When client load the recipe
       Then client receives recipe with name 'Riz sauté'

  Scenario: load recipe with n results
       Given client wants to load recipe by keyword 'soja'
       When client load the recipe
       Then client receives recipe with word 'soja' and count of '2'

   Scenario: rebuild lucene indexes
       Given client wants to rebuild lucene indexes
       When client run rebuild lucene indexes
       Then client is happy to have lucene indexes rebuilt

   Scenario: check if lucene index is rebuilt
       Given client wants to load recipe by keyword 'Xiao'
       When client load the recipe
       Then client receives recipe with name 'Xiao Long Bao'

   Scenario: create tags
       Given client want to create tags
            | name |
            | favori |
            | light |
            | mijoté|
       When client saves tags
       Then client check last tag created

   Scenario: load all tags
       Given client wants to get all tags
       When client loads all tags
       Then client receives all tags, count expected '3'

  Scenario: add tag to recipe
     Given client wants to update recipe by name 'Boeuf bourguignon' and add tag 'favori'
     When client load and update the recipe with tag
     Then client receives updated recipe with tag 'favori'

  Scenario: load recipe by key word and tag name
      Given client wants to load recipe by keyword 'Boeuf bourguignon' and tag name 'favori'
      When client load the recipe with keyword and tag
      Then client receives recipe with name 'Boeuf bourguignon'
      Then client receives updated recipe with tag count '1'

 Scenario: add another tag to recipe
      Given client wants to update recipe by name 'Boeuf bourguignon' and add tag 'mijoté'
      When client load and update the recipe with tag
      Then client receives updated recipe with tag 'mijoté'
      Then client receives updated recipe with tag count '2'

 Scenario: remove tag
      Given client wants to remove tag 'favori' from recipe name 'Boeuf bourguignon'
      When client load recipe and remove tag
      Then client receives updated recipe with tag count '1'