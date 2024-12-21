# ESI-AStar


Implementation of the paper:           
**[Electrophysiological Brain Source Imaging via Combinatorial Search with Provable Optimality](https://ojs.aaai.org/index.php/AAAI/article/view/26471)**

by Guihong Wan, Meng Jiao, Xinglong Ju, Yu Zhang, Haim Schweitzer, Feng Liu.
Published at AAAI 2023.

## Run the code

For example,

java -classpath ".:./lib/hsvis.jar:./lib/jama.jar" AStar

java -classpath ".:./lib/hsvis.jar:./lib/jama.jar" AStar -X data/ESI_X_Region1_SNR10_Exp_25.straight -L data/ESI_L.straight -NeighboringTXT data/ESI_Neighboring_l1.txt -k 11

## Contact
Please contact guihong.wan@outlook.com in case you have any questions

## Cite
Please cite our paper if you use this code in your own work:

'''             
@inproceedings{ESIAStar_AAAI23,                   
  title     = {Electrophysiological brain source imaging via combinatorial search with provable optimality},    
  author    = {Wan, Guihong and Jiao, Meng and Ju, Xinglong and Zhang, Yu and Schweitzer, Haim and Liu, Feng},    
  booktitle = {Proceedings of the AAAI conference on artificial intelligence},    
  volume    = {37},    
  number    = {10},    
  pages     = {12491--12499},    
  year      = {2023}               
}        
'''