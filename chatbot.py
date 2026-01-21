import random
import textwrap

from knowledge_base import TOPICS, TOPIC_ALIASES


class MathTutorChatbot:
    def __init__(self) -> None:
        self.topic_names = sorted(TOPICS.keys())

    def respond(self, user_input: str) -> str:
        normalized = self._normalize(user_input)
        if self._is_exit(normalized):
            return "Good luck with your studies!"
        if self._is_greeting(normalized):
            return self._greeting()
        if "study plan" in normalized or "study" in normalized and "plan" in normalized:
            return self._study_plan(normalized)
        if "practice" in normalized or "quiz" in normalized or "problem" in normalized:
            return self._practice_response(normalized)
        if "example" in normalized:
            return self._example_response(normalized)
        if "define" in normalized or "definition" in normalized or "explain" in normalized:
            return self._explanation_response(normalized)
        if "topic" in normalized or "help" in normalized:
            return self._help()
        return self._fallback(normalized)

    def _normalize(self, text: str) -> str:
        return " ".join(text.lower().strip().split())

    def _is_exit(self, text: str) -> bool:
        return text in {"exit", "quit", "bye", "goodbye"}

    def _is_greeting(self, text: str) -> bool:
        return any(word in text for word in {"hi", "hello", "hey", "hola"})

    def _greeting(self) -> str:
        return (
            "Hi! I can explain topics, generate practice problems, or create study plans. "
            "Try: 'Explain eigenvalues' or 'Practice integration by parts'."
        )

    def _study_plan(self, text: str) -> str:
        topic = self._match_topic(text) or "calculus"
        plan = [
            "Week 1: Refresh prerequisites and core definitions.",
            "Week 2: Work through guided examples and proofs.",
            "Week 3: Solve mixed practice sets and past exam questions.",
            "Week 4: Simulate timed quizzes and review weak areas.",
        ]
        return self._format_response(
            f"4-week study plan for {topic}:", plan
        )

    def _practice_response(self, text: str) -> str:
        topic = self._match_topic(text)
        if not topic:
            return self._format_response(
                "Which topic would you like practice on?", self._topic_list()
            )
        problems = list(TOPICS[topic]["practice"])
        random.shuffle(problems)
        return self._format_response(
            f"Practice problems for {topic}:", problems[:3]
        )

    def _example_response(self, text: str) -> str:
        topic = self._match_topic(text)
        if not topic:
            return self._format_response(
                "Which topic should I show an example for?", self._topic_list()
            )
        example = TOPICS[topic]["example"]
        return self._format_response(
            f"Example: {example['prompt']}", example["solution"]
        )

    def _explanation_response(self, text: str) -> str:
        topic = self._match_topic(text)
        if not topic:
            return self._format_response(
                "I can explain these topics:", self._topic_list()
            )
        summary = TOPICS[topic]["summary"]
        return self._format_response(
            f"{topic.title()} summary:", [summary]
        )

    def _help(self) -> str:
        return self._format_response(
            "You can ask me to:",
            [
                "Explain a topic (e.g., 'Explain limits').",
                "Show a worked example (e.g., 'Example of Bayes').",
                "Generate practice problems (e.g., 'Practice derivatives').",
                "Create a study plan (e.g., 'Study plan for linear algebra').",
            ],
        )

    def _fallback(self, text: str) -> str:
        topic = self._match_topic(text)
        if topic:
            return self._explanation_response(topic)
        return self._format_response(
            "I didn't catch that. Try one of these topics:", self._topic_list()
        )

    def _match_topic(self, text: str) -> str | None:
        if text in TOPICS:
            return text
        for key, canonical in TOPIC_ALIASES.items():
            if key in text:
                return canonical
        for topic in TOPICS:
            if topic in text:
                return topic
        return None

    def _topic_list(self) -> list[str]:
        return [f"- {topic}" for topic in self.topic_names]

    def _format_response(self, title: str, lines: list[str]) -> str:
        wrapped = [textwrap.fill(line, width=78) for line in lines]
        return "\n".join([title, *wrapped])
