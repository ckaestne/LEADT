# Variability Mining with LEADT
Consistent Semi-Automatic Detection of Product-Line Features

## Description

LEADT (short for Location, Expansion, And Documentation Tool) supports developers in locating features in Java code in order to turn them into a software product line. It is built on top of [CIDE](http://fosd.de/cide).

Description: 
Software product line engineering is an efficient means to generate a set of tailored software products from a common implementation. However, adopting a product-line approach poses a major challenge and significant risks, since typically legacy code must be migrated toward a product line. Our aim is to lower the adoption barrier by providing semi-automatic tool support, called feature mining, to support developers in locating, documenting, and extracting implementations of product-line features from legacy code. Feature mining combines prior work on concern location, reverse engineering, and product-line--aware type systems, but is tailored specifically for the use in product lines. Our work extends prior work in three important aspects: (1) we provide a consistency indicator based on a product-line--aware type system, (2) we mine features at a fine level of granularity, and (3) we exploit domain knowledge about the relationship between features. 

LEADT is a research prototype for variability mining.
It was branched from CIDE and should be integrated back
to CIDE at some point.



## Howto
### Installation

LEADT is an Eclipse Plug-in, and currently includes an older version of CIDE (it is currently not compatible with other CIDE versions, but this is planned for near-future releases). To install you need Eclipse 3.5 or 3.6 running with Java 1.5. (Other versions may work but have not been tested). Install the plugin only in an Eclipse version you don't use productively, since LEADT and CIDE are in development status and may affect all projects in the workspace.

Currently, please build the plugins from the source. We will provide binaries again in the future.
(Download the plugins and unzip them in Eclipse's dropins directory.)

### Usage

Switch to the CIDE perspective.

If CIDE is correctly installed you see a CIDE submenu in the context menu of every project (use the "project explorer", not "package explorer" in Eclipse). In this submenu you find options to specify features, generate variants/products for specific feature selections. Here you also find the option "Prepare for feature mining", which is necessary for all further steps. 

Now you can open every file with a special *CIDE editor*. In this editor you can highlight a piece of code and select a feature from the context menu. You should see a colored background on this code segment shortly after. If the "*Colored Java Editor*" (Advanced Java Support) or "*Colored Source Editor*" (for all other languages) is not show in the "Open With" selection, select it using the "Other..." dialog. Tip: In Eclipse preferences (General - Editors - File Association) you can assign certain file types to always open with the CIDE editor.

LEADT provides new views: FeatureManager to select the feature you are currently interested in (all features have to be specified before selecting "prepare for feature mining") and Recommendation Manager, which displays the current recommendations.

You can select recommendations to jump to the according code fragment. When you annotate a code fragment, LEADT updates the recommendations accordingly. Furthermore, you can mark recommendations as incorrect using the recommendation's context menu ("Hide").

For more information on CIDE (without LEADT) see http://fosd.de/cide

## Publications

C. Kästner, A. Dreiling, and K. Ostermann. Variability Mining: Consistent Semiautomatic Detection of Product-Line Features. IEEE Transactions on Software Engineering (TSE), accepted for publication 2013.


## Contact

LEADT was developed by Alexander Dreiling at the University of Magdeburg, Germany and by [Christian Kästner](http://www.cs.cmu.edu/~ckaestne/) who is with Carnegie Mellon University. For information about the project, please contact the development team. 

The source code and case studies are available in LEADT's github repository.




