import * as sdk from 'microsoft-cognitiveservices-speech-sdk';

class SpeechRecognitionService {
  private speechConfig: sdk.SpeechConfig | null = null;
  private recognizer: sdk.SpeechRecognizer | null = null;
  private isInitialized = false;

  // Khởi tạo service với key và region
  public initialize(speechKey: string, serviceRegion: string): boolean {
    try {
      this.speechConfig = sdk.SpeechConfig.fromSubscription(speechKey, serviceRegion);
      this.speechConfig.speechRecognitionLanguage = 'ja-JP';
      this.isInitialized = true;
      return true;
    } catch (error) {
      console.error('Error initializing speech recognition service:', error);
      return false;
    }
  }

  // Bắt đầu nhận dạng giọng nói
  public startRecognition(
    onRecognizing: (text: string) => void,
    onRecognized: (text: string) => void,
    onError: (error: any) => void
  ): void {
    if (!this.isInitialized || !this.speechConfig) {
      onError(new Error('Speech recognition service not initialized'));
      return;
    }

    try {
      const audioConfig = sdk.AudioConfig.fromDefaultMicrophoneInput();
      this.recognizer = new sdk.SpeechRecognizer(this.speechConfig, audioConfig);

      this.recognizer.recognizing = (_, e) => {
        if (e.result.reason === sdk.ResultReason.RecognizingSpeech) {
          onRecognizing(e.result.text);
        }
      };

      this.recognizer.recognized = (_, e) => {
        if (e.result.reason === sdk.ResultReason.RecognizedSpeech) {
          onRecognized(e.result.text);
        } else if (e.result.reason === sdk.ResultReason.NoMatch) {
          onError('No speech could be recognized');
        }
      };

      this.recognizer.canceled = (_, e) => {
        if (e.reason === sdk.CancellationReason.Error) {
          onError(`Error: ${e.errorCode} ${e.errorDetails}`);
        }
      };

      this.recognizer.startContinuousRecognitionAsync(
        () => console.log('Speech recognition started'),
        (error) => onError(error)
      );
    } catch (error) {
      onError(error);
    }
  }

  // Dừng nhận dạng giọng nói
  public stopRecognition(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.recognizer) {
        resolve();
        return;
      }

      this.recognizer.stopContinuousRecognitionAsync(
        () => {
          this.recognizer = null;
          resolve();
        },
        (error) => reject(error)
      );
    });
  }

  // Kiểm tra xem service đã được khởi tạo chưa
  public isServiceInitialized(): boolean {
    return this.isInitialized;
  }
}

export default new SpeechRecognitionService();
