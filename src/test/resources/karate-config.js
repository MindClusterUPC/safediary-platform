function fn() {
  var System = Java.type('java.lang.System');
  var port = karate.properties['local.server.port'] || System.getProperty('local.server.port') || '8080';
  var baseUrl = karate.properties['baseUrl'] || System.getProperty('baseUrl') || ('http://localhost:' + port);
  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 5000);
  return {
    baseUrl: baseUrl
  };
}
