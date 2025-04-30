# Setting Up Your OpenAI API Key

To use the Japanese Speech Analyzer, you need to configure your OpenAI API key for the Whisper speech-to-text functionality.

## Instructions

1. Get an API key from OpenAI:
   - Visit [OpenAI Platform](https://platform.openai.com/)
   - Sign up or log in
   - Navigate to the API Keys section
   - Create a new API key

2. Add your API key to the `.env` file:
   - Open the `.env` file in the `speech-python` directory
   - Replace `your-openai-api-key-here` with your actual OpenAI API key
   - Save the file

Example:
```
OPENAI_API_KEY=sk-abcdefghijklmnopqrstuvwxyz123456789
```

3. Restart the FastAPI server:
   ```bash
   cd speech-python
   source venv/bin/activate
   python3 -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
   ```

## Security Notes

- Never commit your API key to version control
- The `.env` file is included in `.gitignore` to prevent accidental commits
 