import unittest
import json
import os
from httplib2 import Http

class TestEurekaServer(unittest.TestCase):

    def call_eureka_service(self, serviceName):
        # Open 8761 port from ECS security group
        targetUri = "http://{}:8761/eureka/apps/{}".format(containerIP, serviceName)
        http_obj = Http(".cache")
        (resp, content) = http_obj.request(
            uri = targetUri,
            method = 'GET',
            headers = {'Content-Type': 'application/json; charset=UTF-8', 'connection': 'close', 'Accept': 'application/json'}
        )
        return resp, content

    def service_present(self, results, serviceName):
        if results["application"]["name"] == serviceName:
            return True
        False

    def assert_eureka_service(self, serviceName):
        http_obj = Http(".cache")
        (resp, content) = self.call_eureka_service(serviceName)
        results = json.loads(content.decode("utf-8"))
        self.assertEqual(resp.status, 200)
        self.assertEqual(True, self.service_present(results, serviceName))

    def test_licensingservice(self):
        self.assert_eureka_service("LICENSINGSERVICE")

    def test_organizationservice(self):
        self.assert_eureka_service("ORGANIZATIONSERVICE")

    def test_configservice(self):
        self.assert_eureka_service("CONFIGSERVER")

    def test_zuulservice(self):
        self.assert_eureka_service("ZUULSERVICE")

if __name__ == '__main__':
    containerIP = os.getenv('CONTAINER_IP', "0.0.0.0")
    print "Running eureka service platform tests against container ip: {}".format(containerIP)
    unittest.main()