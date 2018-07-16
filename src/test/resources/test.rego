package mn.demo

default allow = false

authorization = {"method": parts[0], "token": parts[1]} { split(input.headers["Authorization"][_], " ", parts) }
token = {"payload": payload } { io.jwt.decode(authorization.token, [_, payload, _]) }

allow {
    authorization.method = "Bearer"
    token.payload.sub = "micronaut-opa-demo"
}
