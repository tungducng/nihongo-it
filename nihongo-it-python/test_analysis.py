"""
Test script to analyze an audio file directly
"""
import os
import shutil
import requests
import json
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Path to our sample file
SAMPLE_PATH = "../speech-java/src/main/resources/samples/hello_friend.wav"
SENTENCE = "こんにちは、友達！"

def test_direct_analysis():
    """Test direct analysis of a sample file"""
    
    # Check if file exists
    if not os.path.exists(SAMPLE_PATH):
        logger.error(f"Sample file not found: {SAMPLE_PATH}")
        return
    
    logger.info(f"Testing with sample file: {SAMPLE_PATH}, size: {os.path.getsize(SAMPLE_PATH)}")
    
    # Create a copy of the file to use for testing
    test_file = "test_sample.wav"
    shutil.copy(SAMPLE_PATH, test_file)
    
    # Create request
    url = "http://localhost:8000/analyze"
    
    files = {
        'audio': (os.path.basename(test_file), open(test_file, 'rb'), 'audio/wav')
    }
    data = {
        'sentence': SENTENCE
    }
    
    logger.info(f"Sending request to: {url}")
    
    try:
        response = requests.post(url, files=files, data=data)
        
        logger.info(f"Response status: {response.status_code}")
        
        if response.status_code == 200:
            result = response.json()
            logger.info(f"Analysis result: {json.dumps(result, indent=2)}")
        else:
            logger.error(f"Error: {response.status_code}, {response.text}")
    except Exception as e:
        logger.error(f"Request error: {str(e)}")
    finally:
        # Clean up
        if os.path.exists(test_file):
            os.unlink(test_file)

if __name__ == "__main__":
    test_direct_analysis() 