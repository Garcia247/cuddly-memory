# Math Learning Chatbot (College & Higher Institutions)

A lightweight, offline-friendly chatbot designed to help college and higher‑institution students learn mathematics. It can:

- Explain core concepts (calculus, linear algebra, probability, statistics, discrete math).
- Generate practice problems with worked solutions.
- Provide quick definitions and example walkthroughs.
- Suggest study plans and topic progressions.

## Quick Start

```bash
python app.py
```

## How It Works

The chatbot uses a small, curated knowledge base of topics and a rules‑based intent matcher to decide how to respond. It does not require internet access or external APIs, making it suitable for local/offline learning environments.

## Example Prompts

- `Explain eigenvalues in simple terms`
- `Give me practice problems for integration by parts`
- `Show an example of Bayes' theorem`
- `Create a 4-week study plan for calculus`

## Files

- `app.py` — CLI entrypoint.
- `chatbot.py` — core chatbot logic.
- `knowledge_base.py` — topic summaries, examples, and practice question templates.

## Extending the Bot

Add or edit topics in `knowledge_base.py` and the bot will automatically use them for explanations, examples, and practice generation.
