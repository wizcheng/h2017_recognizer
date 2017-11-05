

# Transcriber and Transformer for Voice Log

## Get Started

run org.hackathon2017.transriber.Transcriber to start


## APIs:

### /transcribe
Transcribe the source wav to text
Will performs format check, IllegalArgumentException if format do not match
Synchronous call

```
curl -F "file=@OSR_us_000_0034_8k.wav" http://localhost:8081/transcribe
```

Supported format
```
RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 16000 Hz
RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 8000 Hz
```

### /transform
Transform * input to WAV format that will supported by sphinx

```
curl -F "file=@prideandprejudice_01_austen_64kb.mp3" http://localhost:8081/transform > testing.wav
```
