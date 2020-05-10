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

  Scenario: load recipe created before by id
    Given client wants to load recipe created before by id '1'
    When client load the recipe by id
    Then client receives recipe not null

  Scenario: load recipe created before
    Given client wants to load recipe created before by name 'Boeuf bourguignon'
    When client load the recipe
    Then client receives recipe with name 'Boeuf bourguignon'

  Scenario: update recipe created before
    Given client wants to update recipe created before by name 'Boeuf bourguignon'
    When client load and update the recipe with new name, description and comment 'Boeuf bourguignon modified'
    Then client receives updated recipe with name 'Boeuf bourguignon modified'

  Scenario: load recipe modified before
      Given client wants to load recipe created before by name 'Boeuf bourguignon modified'
      When client load the recipe
      Then client receives recipe with name 'Boeuf bourguignon modified'

  Scenario: Create recipe with OCR from recipe binary entity
       Given client wants to create a recipe from image and name 'poulet aux carottes'
       When client save recipe with recipe binary entity
       Then the client receives recipes created with autodescription from image text

  Scenario: Create recipe with OCR from byte array
       Given client wants to create a recipe from image and name 'poulet aux carottes'
       When client save recipe with image as bytes array
       Then the client receives recipes created with autodescription from image text

  Scenario: load compressed light recipe created before by name
     Given client wants to load recipe created before by name 'poulet aux carottes'
     When client loads the light compressed recipe by name
     Then client receives compressed light recipe with name containing 'poulet aux carottes'
