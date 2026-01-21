from chatbot import MathTutorChatbot


def main() -> None:
    bot = MathTutorChatbot()
    print("Math Learning Chatbot — type 'exit' to quit.")
    while True:
        try:
            user_input = input("You: ")
        except EOFError:
            break
        response = bot.respond(user_input)
        print(f"Bot: {response}")
        if response == "Good luck with your studies!":
            break


if __name__ == "__main__":
    main()
