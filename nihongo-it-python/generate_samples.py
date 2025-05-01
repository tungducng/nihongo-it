import os
import argparse
import openai
import pydub
from dotenv import load_dotenv

# Load environment variables (for OpenAI API key)
load_dotenv()

# Ensure API key is set
openai.api_key = os.getenv("OPENAI_API_KEY")
if not openai.api_key:
    raise ValueError("OPENAI_API_KEY environment variable not set! Add it to your .env file.")

# Define sentences with their IDs
SENTENCES = {
    "today_library": "今日、図書館で本を借りました。",
    "hello_friend": "こんにちは、友達！",
    "weather_good": "今日の天気はとても良いですね。",
    "japanese_study": "日本語を勉強することは楽しいです。"
}

# Japanese voice options from OpenAI
VOICES = ["shimmer", "alloy", "echo", "fable", "onyx", "nova"]
DEFAULT_VOICE = "alloy"  # Most neutral voice

def generate_sample(sentence_id, output_dir, voice=DEFAULT_VOICE, rate=1.0):
    """Generate a TTS audio sample for a given sentence ID using OpenAI's TTS"""
    if sentence_id not in SENTENCES:
        print(f"Error: Unknown sentence ID '{sentence_id}'")
        return False
    
    if voice not in VOICES:
        print(f"Warning: Voice '{voice}' not in {VOICES}, using {DEFAULT_VOICE}")
        voice = DEFAULT_VOICE
    
    text = SENTENCES[sentence_id]
    output_path = os.path.join(output_dir, f"{sentence_id}.mp3")
    wav_output_path = os.path.join(output_dir, f"{sentence_id}.wav")
    
    # Create output directory if it doesn't exist
    os.makedirs(output_dir, exist_ok=True)
    
    print(f"Generating audio for: {text}")
    
    try:
        # Create audio using OpenAI TTS
        response = openai.audio.speech.create(
            model="gpt-4o-mini-tts",
            voice=voice,
            input=text
        )
        
        # Save the MP3 file
        response.stream_to_file(output_path)
        print(f"Saved MP3 to {output_path}")
        
        # Convert MP3 to WAV (SpringBoot expects WAV files)
        sound = pydub.AudioSegment.from_mp3(output_path)
        
        # Adjust speed if needed
        if rate != 1.0:
            sound = sound._spawn(sound.raw_data, overrides={
                "frame_rate": int(sound.frame_rate * rate)
            })
        
        # Export as WAV
        sound.export(wav_output_path, format="wav")
        print(f"Saved WAV to {wav_output_path}")
        
        return True
    
    except Exception as e:
        print(f"Error generating audio: {e}")
        return False

def generate_all_samples(output_dir, voice=DEFAULT_VOICE, rate=1.0):
    """Generate samples for all defined sentences"""
    success_count = 0
    for sentence_id in SENTENCES:
        if generate_sample(sentence_id, output_dir, voice, rate):
            success_count += 1
    
    print(f"Generated {success_count} of {len(SENTENCES)} sample files")

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Generate Japanese TTS samples using OpenAI")
    parser.add_argument("--output", "-o", 
                        default="../nihongo-it-backend/src/main/resources/samples",
                        help="Output directory for sample files")
    parser.add_argument("--id", 
                        help="Generate sample for a specific sentence ID")
    parser.add_argument("--voice", default=DEFAULT_VOICE,
                        help=f"Voice to use for TTS. Options: {', '.join(VOICES)}")
    parser.add_argument("--rate", type=float, default=1.0,
                        help="Speed rate adjustment (0.8=slower, 1.2=faster)")
    
    args = parser.parse_args()
    
    if args.id:
        generate_sample(args.id, args.output, args.voice, args.rate)
    else:
        generate_all_samples(args.output, args.voice, args.rate) 