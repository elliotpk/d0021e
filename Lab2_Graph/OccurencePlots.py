import numpy as np
import matplotlib.pyplot as plt
import scipy

f = open("SinkCBR_25Loss.txt", "r")
i = 0.0
j = 1.0
arr = []
temparr = []
temp = 0
for x in f:
    if float(x) >= i and float(x) < j:
        temp += 1
    else:
        arr.append(temp)
        i = i+1
        j = j+1
        temp = 0
f.close()

bins = max(arr) - min(arr) + 1
plt.hist(arr, bins, density=True, alpha=0.5)
plt.xlabel("Packets/second")
plt.ylabel("Probability")

def poissonFit(args):    
    lambda_ = np.mean(args)
    x = np.arange(0, max(args)+1)
    y = scipy.stats.poisson.pmf(x, lambda_)
    plt.plot(x + 0.5, y, '--', label='Poisson fit')

def gaussFit(args):
    [mean_fit, std_fit] = scipy.stats.norm.fit(args)
    x = np.linspace(np.min(args), np.max(args))
    plt.plot(x + 0.5, scipy.stats.norm.pdf(x, mean_fit, std_fit), label='Gaussian Fit')

gaussFit(arr)    
poissonFit(arr)

plt.title("CBR Sink - 25% Loss")
plt.legend()

plt.xlim([min(arr)-5, max(arr)+5])
plt.show()

