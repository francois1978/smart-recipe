Feature: ingredient test

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

Scenario: create ingredients
      Given client wants to create ingredients
            | name |
            | boeuf |
            | poulet |
            | porc |
            | sel |
            | carotte |
            | courgette  |
            | pommes de terre |
      When client save ingredients
      Then client receives ingredients, list size "7"

Scenario: find ingredient by name
      Given client wants to find ingredient by name "cOurgette"
      When client load ingredient
      Then client receives ingredients, list size "1"

Scenario: find ingredients in recipe
      Given client wants to find ingredient in recipe with id '1'
      When client load ingredient for the recipe
      Then client receives ingredients as string, list size "2"
