import unittest
import json
import os
from httplib2 import Http

class TestConfigServer(unittest.TestCase):

    def call_config_service(self, serviceName, serviceEnv):
        targetUri = "http://{}:5555/api/configserver/{}/{}".format(containerIP, serviceName, serviceEnv)
        http_obj = Http(".cache")
        (resp, content) = http_obj.request(
            uri=targetUri,
            method='GET',
            headers={'Content-Type': 'application/json; charset=UTF-8', 'connection': 'close'}
        )
        return resp, content

    def test_licensingservice_aws_dev(self):
        http_obj = Http(".cache")
        (resp, content) = self.call_config_service("licensingservice", "aws-dev")
        results = json.loads(content.decode("utf-8"))
        self.assertEqual(resp.status, 200)
        self.assertEqual("https://github.com/jzzzin/spring-micro-study/licensingservice/licensingservice-aws-dev.yml",
                         results["propertySources"][0]["name"])

    def test_licensingservice_default(self):
        http_obj = Http(".cache")
        (resp, content) = self.call_config_service("licensingservice", "default")
        results = json.loads(content.decode("utf-8"))
        self.assertEqual(resp.status, 200)
        self.assertEqual("https://github.com/jzzzin/spring-micro-study/licensingservice/licensingservice.yml",
                         results["propertySources"][0]["name"])

    def test_organizationservice_aws_dev(self):
        http_obj = Http(".cache")
        (resp, content) = self.call_config_service("organizationservice", "aws-dev")
        results = json.loads(content.decode("utf-8"))
        self.assertEqual(resp.status, 200)
        self.assertEqual("https://github.com/jzzzin/spring-micro-study/organizationservice/organizationservice-aws-dev.yml",
                         results["propertySource"][0]["name"])

    def test_organizationservice_default(self):
        http_obj = Http(".cache")
        (resp, content) = self.call_config_service("organizationservice", "default")
        results = json.loads(content.decode("utf-8"))
        self.assertEqual(resp.status, 200)
        self.assertEqual("https://github.com/jzzzin/spring-micro-study/organizationservice/organizationservice.yml",
                         results["propertySource"][0]["name"])

if __name__ == '__main__':
    containerIP = os.getenv('CONTAINER_IP', "0.0.0.0")
    print "Running config service platform tests against container ip: {}".format(containerIP)
    unittest.main()