t_all = [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]


P1 = [0,0]
P2 = [5,5]
P3 = [0,10]
all_points = []
for t in t_all:
    x = (1 - t)*(1 - t)*P1[0] + 2 * (1 - t) *t*P2[0] + t*P3[0]
    y = (1 - t)*(1 - t)*P1[1] + 2 * (1 - t) *t*P2[1] + t*P3[1]
    all_points.append([x,y])
for i in all_points:
    print(i)
a = 5.1
print(a%int(a))