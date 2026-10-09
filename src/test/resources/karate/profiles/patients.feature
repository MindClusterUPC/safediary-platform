# User Story: US-006 - Registro de Pacientes y Contactos de Emergencia
Feature: Gestión y consulta de perfiles de pacientes con contacto de emergencia

  Background:
    * url baseUrl + '/api/v1/patients'

  Scenario: Registrar un nuevo paciente, consultar por ID y actualizar contacto de emergencia
    # 1. Crear nuevo paciente (201)
    Given request
    """
    {
      "firstName": "Carlos",
      "lastName": "Mendoza",
      "email": "carlos.mendoza.bdd@example.com",
      "password": "Password123*",
      "emergencyContactName": "Maria Mendoza",
      "emergencyContactRelationship": "PARENT",
      "emergencyContactPhone": "+51987654321",
      "emergencyContactEmail": "maria.bdd@example.com"
    }
    """
    When method post
    Then status 201
    And match response.id == '#number'
    And match response.email == 'carlos.mendoza.bdd@example.com'
    * def patientId = response.id

    # 2. Consultar paciente por ID (200)
    Given path patientId
    When method get
    Then status 200
    And match response.id == patientId
    And match response.firstName == 'Carlos'

    # 3. Actualizar contacto de emergencia (200)
    Given path patientId, 'emergency-contact'
    And request
    """
    {
      "name": "Ana Gomez",
      "relationship": "SPOUSE",
      "phoneNumber": "+51912345678",
      "email": "ana.gomez@example.com"
    }
    """
    When method put
    Then status 200
    And match response.emergencyContact.name == 'Ana Gomez'
    And match response.emergencyContact.relationship == 'SPOUSE'

  Scenario: Consultar un paciente inexistente retorna estado 404
    Given path 999999
    When method get
    Then status 404
