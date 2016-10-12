#ifndef ONYXPDFWRITER_H
#define ONYXPDFWRITER_H

#include <vector>
#include <string>
#include <memory>

class PageScribble;
class PageAnnotation;

class OnyxPdfWriter
{
public:
    OnyxPdfWriter();
    ~OnyxPdfWriter();

    bool openPDF(const std::string &path);
    bool saveAs(const std::string &path, bool savePagesWithAnnotation);
    void close();

    bool isOpened() const;
    bool writeScribble(const PageScribble &scribble);
    bool writeScribbles(const std::vector<PageScribble> &scribbles);
    bool writeAnnotation(const PageAnnotation &annotation);
    bool writeAnnotations(const std::vector<PageAnnotation> &annotations);

private:
    class Impl;
    std::unique_ptr<Impl> impl;
};

#endif // ONYXPDFWRITER_H
