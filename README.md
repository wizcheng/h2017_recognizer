



Start:

run org.hackathon2017.transriber.Transcriber


APIs:

/transcribe
Transcribe the source wav to text
Will performs format check, IllegalArgumentException if format do not match
Synchronous call

curl -F "file=@OSR_us_000_0034_8k.wav" localhost:8081/transcribe


/transform
Transform * input to WAV format that will supported by sphinx

curl -F "file=@prideandprejudice_01_austen_64kb.mp3" localhost:8081/transform > testing.wav