# import streamlit as st
# import torchaudio
# import torch
# from transformers import Wav2Vec2ForCTC, Wav2Vec2Tokenizer, pipeline
#
#
# # Load pre-trained models for transcription and emotion recognition
# @st.cache_resource
# def load_models():
#     tokenizer = Wav2Vec2Tokenizer.from_pretrained("facebook/wav2vec2-base-960h")
#     model = Wav2Vec2ForCTC.from_pretrained("facebook/wav2vec2-base-960h")
#     emotion_classifier = pipeline("text-classification", model="j-hartmann/emotion-english-distilroberta-base",
#                                   return_all_scores=True)
#     return tokenizer, model, emotion_classifier
#
#
# tokenizer, model, emotion_classifier = load_models()
#
#
# # Function to transcribe audio
# def transcribe_audio(audio_data):
#     input_values = tokenizer(audio_data, return_tensors="pt", padding="longest").input_values
#     logits = model(input_values).logits
#     predicted_ids = torch.argmax(logits, dim=-1)
#     transcription = tokenizer.batch_decode(predicted_ids)[0].lower()
#     return transcription
#
#
# # Streamlit app interface
# st.title("Emotion Recognition App")
#
# # Record or upload audio file
# audio_file = st.file_uploader("Upload an audio file", type=["wav", "mp3"])
#
# if audio_file is not None:
#     # Load the audio file
#     waveform, sample_rate = torchaudio.load(audio_file)
#
#     # Transcribe the audio
#     with st.spinner('Transcribing audio...'):
#         transcription = transcribe_audio(waveform.squeeze().numpy())
#
#     st.write("Transcription:")
#     st.write(transcription)
#
#     # Perform emotion recognition
#     with st.spinner('Analyzing emotion...'):
#         emotion_scores = emotion_classifier(transcription)
#         emotion = max(emotion_scores[0], key=lambda x: x['score'])
#
#     st.write("Detected Emotion:")
#     st.write(f"**{emotion['label']}** with a confidence of **{emotion['score']:.2f}**")
#
#     # Display emotion scores
#     st.write("Emotion Scores:")
#     for score in emotion_scores[0]:
#         st.write(f"{score['label']}: {score['score']:.2f}")
#


import torch

print("PyTorch version:", torch.__version__)
print("Is CUDA available?", torch.cuda.is_available())  # This should return `False`
