TOPICS = {
    "limits": {
        "summary": "Limits describe the behavior of a function as the input approaches a value.",
        "example": {
            "prompt": "Compute lim_{x→2} (x^2 - 4)/(x-2).",
            "solution": [
                "Factor numerator: x^2 - 4 = (x-2)(x+2).",
                "Cancel (x-2) for x ≠ 2.",
                "Evaluate x+2 at x=2: 4.",
            ],
        },
        "practice": [
            "Compute lim_{x→3} (x^2 - 9)/(x-3).",
            "Find lim_{x→0} (sin x)/x.",
            "Evaluate lim_{x→∞} (2x^2 + 1)/(x^2 - 3).",
        ],
    },
    "derivatives": {
        "summary": "Derivatives measure instantaneous rates of change and the slope of a function.",
        "example": {
            "prompt": "Find the derivative of f(x)=x^3 - 5x.",
            "solution": [
                "Use the power rule: d/dx x^3 = 3x^2.",
                "Derivative of -5x is -5.",
                "So f'(x)=3x^2-5.",
            ],
        },
        "practice": [
            "Differentiate f(x)=sqrt(x) + 1/x.",
            "Find f'(x) for f(x)=e^x cos x.",
            "Compute the derivative of f(x)=ln(x^2+1).",
        ],
    },
    "integration by parts": {
        "summary": "Integration by parts converts ∫u dv into u·v - ∫v du.",
        "example": {
            "prompt": "Evaluate ∫ x e^x dx.",
            "solution": [
                "Let u = x, dv = e^x dx.",
                "Then du = dx and v = e^x.",
                "So ∫ x e^x dx = x e^x - ∫ e^x dx = x e^x - e^x + C.",
            ],
        },
        "practice": [
            "Compute ∫ x sin x dx.",
            "Evaluate ∫ x ln x dx.",
            "Find ∫ x^2 e^x dx.",
        ],
    },
    "linear algebra": {
        "summary": "Linear algebra studies vectors, matrices, and linear transformations.",
        "example": {
            "prompt": "Solve the system: x + y = 3, 2x - y = 1.",
            "solution": [
                "Add equations to eliminate y: 3x = 4.",
                "So x = 4/3.",
                "Substitute: y = 3 - 4/3 = 5/3.",
            ],
        },
        "practice": [
            "Compute the determinant of [[1,2],[3,4]].",
            "Find eigenvalues of [[2,0],[0,3]].",
            "Solve Ax=b for A=[[1,1],[2,3]] and b=[4,9].",
        ],
    },
    "eigenvalues": {
        "summary": "Eigenvalues are scalars λ such that A v = λ v for a nonzero vector v.",
        "example": {
            "prompt": "Find eigenvalues of A=[[2,1],[1,2]].",
            "solution": [
                "Compute det(A-λI)=det([[2-λ,1],[1,2-λ]]).",
                "(2-λ)^2 - 1 = 0 → (2-λ)^2=1.",
                "So 2-λ=±1 → λ=1 or 3.",
            ],
        },
        "practice": [
            "Find eigenvalues of [[4,0],[0,5]].",
            "Compute eigenvalues of [[0,1],[-2,3]].",
            "Determine eigenvalues of [[1,2],[2,1]].",
        ],
    },
    "probability": {
        "summary": "Probability quantifies uncertainty, ranging from 0 (impossible) to 1 (certain).",
        "example": {
            "prompt": "Two fair coins are flipped. What is P(exactly one head)?",
            "solution": [
                "Sample space: HH, HT, TH, TT.",
                "Exactly one head: HT, TH (2 outcomes).",
                "Probability = 2/4 = 1/2.",
            ],
        },
        "practice": [
            "Compute P(A∩B) if P(A)=0.5, P(B)=0.6, P(A∪B)=0.8.",
            "Find the expected value of a fair six-sided die.",
            "What is P(at least one 6) in two dice rolls?",
        ],
    },
    "bayes": {
        "summary": "Bayes' theorem updates probabilities using evidence: P(A|B)=P(B|A)P(A)/P(B).",
        "example": {
            "prompt": "A test is 99% accurate; 1% of people have a disease. Compute P(disease|positive).",
            "solution": [
                "P(D)=0.01, P(+|D)=0.99, P(+|¬D)=0.01.",
                "P(+)=0.99*0.01 + 0.01*0.99 = 0.0198.",
                "So P(D|+)=0.0099/0.0198=0.5.",
            ],
        },
        "practice": [
            "Given P(A)=0.2, P(B|A)=0.7, P(B)=0.5, find P(A|B).",
            "A spam filter flags 2% of good emails and 95% of spam. If 10% emails are spam, compute P(spam|flag).",
            "Use Bayes to update P(A) after observing evidence B.",
        ],
    },
    "statistics": {
        "summary": "Statistics focuses on data collection, analysis, inference, and variability.",
        "example": {
            "prompt": "Compute the mean of 2, 4, 6, 8.",
            "solution": [
                "Sum = 20.",
                "Mean = 20/4 = 5.",
            ],
        },
        "practice": [
            "Find the variance of 1, 2, 3.",
            "Compute the median of 3, 7, 8, 10, 12.",
            "Explain the difference between correlation and causation.",
        ],
    },
    "discrete math": {
        "summary": "Discrete math covers countable structures like graphs, sets, and logic.",
        "example": {
            "prompt": "How many subsets does a set with 4 elements have?",
            "solution": [
                "A set with n elements has 2^n subsets.",
                "So 2^4 = 16.",
            ],
        },
        "practice": [
            "Determine the number of edges in a complete graph K5.",
            "Evaluate the truth of (P→Q) when P is true and Q is false.",
            "Compute 7P3 and 7C3.",
        ],
    },
}

TOPIC_ALIASES = {
    "calc": "limits",
    "calculus": "limits",
    "derivative": "derivatives",
    "integration": "integration by parts",
    "linear": "linear algebra",
    "matrix": "linear algebra",
    "eigenvalue": "eigenvalues",
    "bayes theorem": "bayes",
    "stats": "statistics",
    "stat": "statistics",
    "prob": "probability",
    "discrete": "discrete math",
}
