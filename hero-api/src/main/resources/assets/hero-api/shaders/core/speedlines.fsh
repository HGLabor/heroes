#version 330

uniform float Radius;
uniform float Edge;
uniform float GameTime;
uniform vec2 ScreenSize;

out vec4 fragColor;

const vec3 COLOR = vec3(1, 1, 1);

vec3 random3(vec3 c) {
    float j = 4096.0*sin(dot(c,vec3(17.0, 59.4, 15.0)));
    vec3 r;
    r.z = fract(512.0*j);
    j *= .125;
    r.x = fract(512.0*j);
    j *= .125;
    r.y = fract(512.0*j);
    return r-0.5;
}

float simplex3d(vec3 p) {
    vec3 s = floor(p + dot(p, vec3(0.3333333)));
    vec3 x = p - s + dot(s, vec3(0.1666667));
    vec3 e = step(vec3(0.0), x - x.yzx);
    vec3 i1 = e*(1.0 - e.zxy);
    vec3 i2 = 1.0 - e.zxy*(1.0 - e);
    vec3 x1 = x - i1 + 0.1666667;
    vec3 x2 = x - i2 + 2.0*0.1666667;
    vec3 x3 = x - 1.0 + 3.0*0.1666667;
    vec4 w, d;
    w.x = dot(x, x);
    w.y = dot(x1, x1);
    w.z = dot(x2, x2);
    w.w = dot(x3, x3);
    w = max(0.6 - w, 0.0);
    d.x = dot(random3(s), x);
    d.y = dot(random3(s + i1), x1);
    d.z = dot(random3(s + i2), x2);
    d.w = dot(random3(s + 1.0), x3);
    w *= w;
    w *= w;
    d *= w;
    return dot(d, vec4(52.0));
}

void main()
{
    float time = (GameTime * 1200) * 2.;
    float scale = 50.0;
    vec2 uv = (gl_FragCoord.xy*2. - ScreenSize.xy) / ScreenSize.y * 0.5;
    vec2 p = vec2(0.5*ScreenSize.x/ScreenSize.y, 0.5) + normalize(uv) * min(length(uv), 0.05);
    vec3 p3 = scale*0.25*vec3(p.xy, 0) + vec3(0, 0, time*0.025);
    float noise = simplex3d(p3 * 32.0) * 0.5 + 0.5;
    float dist = abs(clamp(length(uv)/Radius, 0.0, 1.0)*noise*2.-1.);
    float stepped = smoothstep(Edge-.5,Edge+.5, noise * (1.0-pow(dist, 4.0)));
    float final = smoothstep(Edge - 0.05, Edge + 0.05, noise*stepped);

    fragColor = vec4(COLOR,final);
}