Feature: recipe test

 Scenario: delete all for test data reset
     Given client wants to delete all recipes to reset test data
     When client run delete all recipes
     Then client got no recipe with find all

  Scenario: create n recipes to prepare search testing
     Given client want to create a recipe
          | name | description | comment |
          | Recette1   | boeuf carotte sel | comment  |
          | Recette2   | Courgette celeri sel | comment  |
          | Recette3   | pommes carotte sel | comment  |
          | Recette4   | Olive brocolis poivre  | comment  |
          | Recette5 olive | courgette carotte sel menth | comment  |
          | Recette6 basilic | Courgette porc sel curry | comment  |
          | Recette7   | Courgette olive sel  | comment  |
          | Recette8 courgette | poulet riz sel  | comment  |
          | Recette9 courgette | petits pois navet sel olive melo | comment  |
          | Recette10 porc | carotte sel | comment  |
          | Recette11 courgette olive porc | Courgette carotte sel melon basilic | comment  |
     When client save recipe
     Then the client receives recipes created

  Scenario: check recipes order returned with 2 words
     Given client wants to load recipe by keyword 'courgette olive'
     When client load the recipe
     Then client receives recipes with '4' first ones with names in 'Recette5,Recette7,Recette9,Recette11'

  Scenario: check recipes order returned with 3 words
     Given client wants to load recipe by keyword 'courgette olive navet'
     When client load the recipe
     Then client receives recipes with '1' first ones with names in 'Recette9'

  Scenario: check recipes order returned with 3 words and approximation
     Given client wants to load recipe by keyword 'courgette olive menthe'
     When client load the recipe
     Then client receives recipes with '1' first ones with names in 'Recette5'

 Scenario: check recipes order returned with 3 words and approximation or exact word
     Given client wants to load recipe by keyword 'courgette olive melon'
     When client load the recipe
     Then client receives recipes with '2' first ones with names in 'Recette11, Recette9'

 Scenario: check recipes order returned with 3 words mixing recipes
      Given client wants to load recipe by keyword 'courgette celeri navet'
      When client load the recipe
      Then client receives recipes with '2' first ones with names in 'Recette9, Recette2'

 Scenario: check recipes exact order returned with 3 words mixing recipes
      Given client wants to load recipe by keyword 'porc basilic curry'
      When client load the recipe
      Then client receives recipes with '3' first ones with names in exact order 'Recette6, Recette11, Recette10'



