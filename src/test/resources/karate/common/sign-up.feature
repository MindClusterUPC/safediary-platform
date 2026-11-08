@ignore
Feature: Registro de una cuenta de prueba para obtener un token de acceso

  Scenario: Crear una cuenta de paciente y devolver su token
    * def email = 'karate.' + java.util.UUID.randomUUID() + '@example.com'
    Given url baseUrl + '/api/v1/authentication/sign-up'
    And request { email: '#(email)', password: 'Karate2026*Seguro', role: 'PATIENT' }
    When method post
    Then status 201
    * def token = response.accessToken
    * def accountId = response.accountId
