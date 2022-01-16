import unittest
import requests
import subprocess


container_name = 'android_logs_test_container'


class AndroidLogsServerTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls) -> None:
        subprocess.run(['docker', 'run', '-p', '5000:5000', '-d', '--rm', '--name', container_name, 'tyrrr/android_logs:latest'])


    def test_single_endpoint_accepts_proper_data(self):
        data = {
            'pid': 1,
            'line': 'I/test message logged'
        }
        response = self.make_post_request('/single', data)
        self.assertEqual(200, response.status_code)


    def test_pack_endpoint_accepts_proper_data(self):
        data = {
            'pid': 1,
            'lines': [
                'I/test message logged',
                'E/Another test message 101!'
            ]
        }
        response = self.make_post_request('/pack', data)
        self.assertEqual(200, response.status_code)


    def test_single_endpoint_responds_with_error_to_data_with_wrong_content(self):
        data = {
            'pid': 1
        }
        response = self.make_post_request('/single', data)
        self.assertEqual(400, response.status_code)


    def test_pack_endpoint_responds_with_error_to_data_with_wrong_content(self):
        data = {
            'pid': 1,
            'line': 'I/test message logged'
        }
        response = self.make_post_request('/pack', data)
        self.assertEqual(400, response.status_code)


    def test_single_endpoint_responds_with_error_to_malformed_data(self):
        data = """{
            'pid': 1,
            'line': 'I/test message logged'"""
        response = self.make_post_request('/single', data)
        self.assertEqual(400, response.status_code)


    def test_pack_endpoint_responds_with_error_to_malformed_data(self):
        data = """{
            'pid': 1
            'line': 'I/test message logged'
        }"""
        response = self.make_post_request('/pack', data)
        self.assertEqual(400, response.status_code)


    @classmethod
    def tearDownClass(cls) -> None:
        subprocess.run(['docker', 'stop', container_name])


    def make_post_request(self, endpoint, data):
        url = 'http://127.0.0.1:5000' + endpoint
        return requests.post(url, json=data)
