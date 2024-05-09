curl -X POST --location "http://51.250.103.29:8080/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
          \"username\": \"poma12390\",
          \"password\": \"qwerty\"
        }"


curl -X GET --location "http://51.250.103.29:8080/api/rooms/home/" \
    -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InBvbWExMjM5MCIsImh1YklkIjoyLCJleHAiOjE3MTUwMTY2ODN9.ueTiWMsdHya4t958MPH4gL-VB5NvOllfmVPmlBgw87U"


curl -X PATCH --location "http://51.250.103.29:8080/api/switches/{id}" \
    -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6InBvbWExMjM5MCIsImh1YklkIjoyLCJleHAiOjE3MTUwMTY2ODN9.ueTiWMsdHya4t958MPH4gL-VB5NvOllfmVPmlBgw87U" \
    -H "Content-Type: application/json" \
    -d "{
          \"enabled\": \"true\",
        }"