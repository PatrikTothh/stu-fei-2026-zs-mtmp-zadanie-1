from flask import Flask, jsonify, request
import math

app = Flask(__name__)


@app.route("/")
def home():
    return "Trajectory server is running"

@app.route("/test")
def test():
    return "POST/GET server connection works"

@app.route("/post-test", methods=["POST"])
def post_test():
    print(">>> POST TEST RECEIVED")
    return "POST works"

@app.route("/form")
def form():
    return """
    <html>
        <body>
            <form method="POST" action="/post-test">
                <button type="submit">Send POST</button>
            </form>
        </body>
    </html>
    """

@app.route("/calculate", methods=["POST"])
def calculate_trajectory():
    print(">>> /calculate endpoint reached")

    data = request.get_json()

    print(">>> received data:", data)
    
    data = request.get_json()

    initial_velocity = data.get("initial_velocity")
    angle_degrees = data.get("angle")

    if initial_velocity is None or angle_degrees is None:
        return jsonify({
            "success": False,
            "error": "initial_velocity and angle are required"
        }), 400

    try:
        initial_velocity = float(initial_velocity)
        angle_degrees = float(angle_degrees)
    except (TypeError, ValueError):
        return jsonify({
            "success": False,
            "error": "initial_velocity and angle must be numbers"
        }), 400

    if initial_velocity <= 0:
        return jsonify({
            "success": False,
            "error": "initial_velocity must be greater than 0"
        }), 400

    if angle_degrees <= 0 or angle_degrees >= 90:
        return jsonify({
            "success": False,
            "error": "angle must be greater than 0 and less than 90"
        }), 400

    gravity = 9.81
    angle_radians = math.radians(angle_degrees)

    time_of_flight = (
        2 * initial_velocity * math.sin(angle_radians)
    ) / gravity

    time_step = 0.1

    trajectory_points = []

    current_time = 0.0

    trajectory_points.append({
        "time": 0.0,
        "x": 0.0,
        "y": 0.0
    })

    while current_time < time_of_flight:
        current_time += time_step

        if current_time > time_of_flight:
            current_time = time_of_flight

        x = (
            initial_velocity
            * math.cos(angle_radians)
            * current_time
        )

        y = (
            initial_velocity
            * math.sin(angle_radians)
            * current_time
            - 0.5 * gravity * current_time ** 2
        )

        if y >= 0:
            trajectory_points.append({
                "time": current_time,
                "x": x,
                "y": y
            })

    return jsonify({
        "success": True,
        "points": trajectory_points
    })


if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=8080,
        debug=True
    )