# User Story: US-009 - Gestión de Rutinas Diarias de Bienestar y Hábitos
Feature: Administración de rutinas diarias de los pacientes

  Background:
    * url baseUrl + '/api/v1/daily-routines'

  Scenario: Crear una rutina diaria, listar por paciente y alternar estado activo
    # 1. Crear rutina diaria (201)
    Given request
    """
    {
      "patientId": 101,
      "title": "Meditacion y respiracion matutina",
      "frequencyDays": ["MONDAY", "WEDNESDAY", "FRIDAY"],
      "isNotificationActive": true
    }
    """
    When method post
    Then status 201
    And match response.id == '#number'
    And match response.title == 'Meditacion y respiracion matutina'
    And match response.isActive == true
    * def routineId = response.id

    # 2. Listar rutinas por paciente (200)
    Given path 'patient', 101
    When method get
    Then status 200
    And match response == '#[1]'
    And match response[0].id == routineId

    # 3. Alternar estado activo de la rutina (200)
    Given path routineId, 'toggle-active'
    When method patch
    Then status 200
    And match response.id == routineId
    And match response.isActive == false

  Scenario: Intentar crear una rutina con datos inválidos retorna estado 400
    Given request
    """
    {
      "patientId": null,
      "title": "",
      "frequencyDays": [],
      "isNotificationActive": null
    }
    """
    When method post
    Then status 400
