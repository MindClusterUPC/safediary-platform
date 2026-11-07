# User Story: US-014 - Directorio y Búsqueda de Psicólogos Clínicos Verificados
Feature: Consulta del directorio público de profesionales de la salud mental

  Background:
    * url baseUrl + '/api/v1/clinicians'

  Scenario: Buscar psicólogos en el directorio retorna respuesta exitosa con listado
    When method get
    Then status 200
    And match response == '#[]'

  Scenario: Consultar el perfil público de un psicólogo inexistente retorna 404
    Given path 999999
    When method get
    Then status 404
